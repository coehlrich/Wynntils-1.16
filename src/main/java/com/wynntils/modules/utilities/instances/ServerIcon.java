/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.instances;

import com.wynntils.McIf;
import com.wynntils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.ServerPinger;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.Validate;

import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Consumer;

public class ServerIcon {

    // Fallback resource when waiting for icon or the icon is invalid
    public static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");

    private static final ServerPinger pinger = new ServerPinger();
    private static final List<WeakReference<ServerIcon>> instances = new ArrayList<>();

    private final ServerData server;
    private String lastIcon = null;
    private final ResourceLocation serverIcon;
    private DynamicTexture icon;
    private List<Consumer<ServerIcon>> onDone = new ArrayList<>();
    private boolean allowStale;

    public ServerIcon(ServerData server, boolean allowStale) {
        this.server = server;
        this.allowStale = allowStale;

        serverIcon = new ResourceLocation("servers/" + server.ip + "/icon");
        icon = (DynamicTexture) McIf.mc().getTextureManager().getTexture(serverIcon);

        synchronized (ServerIcon.class) {
            instances.add(new WeakReference<>(this));
        }
    }

    public DynamicTexture getIcon() {
        return icon;
    }

    public ServerData getServer() {
        return server;
    }

    public static synchronized void ping() {
        pinger.tick();

        if (instances.isEmpty()) return;

        Iterator<WeakReference<ServerIcon>> it = instances.iterator();
        while (it.hasNext()) {
            WeakReference<ServerIcon> ref = it.next();
            ServerIcon i = ref.get();
            if (i == null) {
                it.remove();
                continue;
            }
            i.pingImpl();
            if (i.getServerIcon() != null && i.allowStale) {
                it.remove();
            }
        }

        pinger.tick();
    }

    private void pingImpl() {
        if (!server.pinged) {
            if (allowStale && getServerIcon() != null) {
                server.pinged = true;
                return;
            }
            server.pinged = true;
            server.ping = -2L;
            server.motd = StringTextComponent.EMPTY;
            server.playerList = Collections.EMPTY_LIST;
            try {
                pinger.pingServer(server, () -> {});
            } catch (UnknownHostException ignored) {
                server.ping = -1L;
                server.motd = new TranslationTextComponent("multiplayer.status.cannot_resolve")
                        .withStyle(TextFormatting.DARK_RED);
            } catch (Exception ignored) {
                server.ping = -1L;
                server.motd = new TranslationTextComponent("multiplayer.status.cannot_connect")
                        .withStyle(TextFormatting.DARK_RED);
            }
        }
    }

    public synchronized void onDone(Consumer<ServerIcon> c) {
        if (isDone()) {
            c.accept(this);
        }
        onDone.add(c);
    }

    public synchronized boolean isDone() {
        return Objects.equals(server.getIconB64(), lastIcon);
    }

    private synchronized void onDone() {
        onDone.forEach(c -> c.accept(this));
    }

    public void delete() {
        synchronized (ServerIcon.class) {
            instances.removeIf(r -> {
                ServerIcon i = r.get();
                return i == null || i == ServerIcon.this;
            });
        }
    }

    // Modified from net.minecraft.client.gui.screen.ServerListEntryNormal$prepareServerIcon
    public synchronized ResourceLocation getServerIcon() {
        String currentIcon = server.getIconB64();
        if (Objects.equals(currentIcon, lastIcon)) return icon == null ? null : serverIcon;

        lastIcon = currentIcon;

        if (currentIcon == null) {
            McIf.mc().getTextureManager().release(serverIcon);
            icon = null;
            onDone();
            return null;
        }

        try {
            NativeImage nativeimage = NativeImage.fromBase64(currentIcon);
            Validate.validState(nativeimage.getWidth() == 64, "Must be 64 pixels wide");
            Validate.validState(nativeimage.getHeight() == 64, "Must be 64 pixels high");
            if (this.icon == null) {
                this.icon = new DynamicTexture(nativeimage);
            } else {
                this.icon.setPixels(nativeimage);
                this.icon.upload();
            }

            Minecraft.getInstance().getTextureManager().register(serverIcon, this.icon);
            onDone();
            return serverIcon;
        } catch (Throwable throwable) {
            Reference.LOGGER.error("Invalid icon for server " + server.name + " (" + server.ip + ")", throwable);
            server.setIconB64(null);
            onDone();
            return null;
        }
    }

}
