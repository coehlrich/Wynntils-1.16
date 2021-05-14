/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.realms.RealmsBridge;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.util.Locale;
import java.util.Objects;

public class ServerUtils {

    public static void connect(ServerData serverData) {
        connect(serverData, true);
    }

    public static void connect(ServerData serverData, boolean unloadCurrentServerResourcePack) {
        connect(new GuiMultiplayer(new GuiMainMenu()), serverData, unloadCurrentServerResourcePack);
    }

    public static void connect(Screen backGui, ServerData serverData) {
        connect(backGui, serverData, false);
    }

    /**
     * Connect to a server, possibly disconnecting if already on a world.
     *
     * @param backGui GUI to return to on failure
     * @param serverData The server to connect to
     * @param unloadCurrentServerResourcePack If false, retain the same server resource pack between disconnecting and connecting
     */
    public static void connect(Screen backGui, ServerData serverData, boolean unloadCurrentServerResourcePack) {
        disconnect(false, unloadCurrentServerResourcePack);
        FMLClientHandler.instance().connectToServer(backGui, serverData);
    }

    public static void disconnect() {
        disconnect(true);
    }

    public static void disconnect(boolean switchGui) {
        disconnect(switchGui, false);
    }

    /**
     * Disconnect from the current server
     *
     * @param switchGui If true, the current gui is changed (to the main menu in singleplayer, or multiplayer gui)
     * @param unloadServerPack if false, disconnect without refreshing resources by unloading the server resource pack
     */
    public static void disconnect(boolean switchGui, boolean unloadServerPack) {
        WorldClient world = McIf.world();
        if (world == null) return;

        boolean singlePlayer = McIf.mc().isIntegratedServerRunning();
        boolean realms = !singlePlayer && McIf.mc().isConnectedToRealms();

        world.sendQuittingDisconnectingPacket();
        if (unloadServerPack) {
            McIf.mc().loadWorld(null);
        } else {
            loadWorldWithoutUnloadingServerResourcePack(null);
        }

        if (!switchGui) return;
        if (singlePlayer) {
            McIf.mc().displayGuiScreen(new GuiMainMenu());
        } else if (realms) {
            // Should not be possible because Wynntils will
            // never be running on the latest version of Minecraft
            new RealmsBridge().switchToRealms(new GuiMainMenu());
        } else {
            McIf.mc().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }
    }

    public static void loadWorldWithoutUnloadingServerResourcePack(WorldClient world) {
        loadWorldWithoutUnloadingServerResourcePack(world, "");
    }

    private static class FakeResourcePackRepositoryHolder {
        // Will only be created by classloader when used
        static final ResourcePackRepository instance = new ResourcePackRepository(McIf.mc().getResourcePackRepository().getDirResourcepacks(), null, null, null, McIf.mc().options) {
            @Override
            public void clearResourcePack() {
                // Don't
            }
        };
    }

    public static synchronized void loadWorldWithoutUnloadingServerResourcePack(WorldClient world, String loadingMessage) {
        ResourcePackRepository original = McIf.mc().getResourcePackRepository();

        ReflectionFields.Minecraft_resourcePackRepository.setValue(McIf.mc(), FakeResourcePackRepositoryHolder.instance);
        McIf.mc().loadWorld(world, loadingMessage);
        ReflectionFields.Minecraft_resourcePackRepository.setValue(McIf.mc(), original);
    }

    public static ServerData getWynncraftServerData(boolean addNew) {
        return getWynncraftServerData(new ServerList(Minecraft.getInstance()), addNew, Reference.ServerIPS.GAME);
    }

    public static ServerData getWynncraftServerData(ServerList serverList, boolean addNew) {
        return getWynncraftServerData(serverList, addNew, Reference.ServerIPS.GAME);
    }

    /**
     * @param serverList A ServerList
     * @param addNew If true and no server data is found, the newly created server data will be added and saved.
     * @param ip The ip to use if not found
     * @return The server data in the given serverList for Wynncraft (Or a new one if none are found)
     */
    public static ServerData getWynncraftServerData(ServerList serverList, boolean addNew, String ip) {
        ServerData server = null;

        int i = 0, count = serverList.countServers();
        for (; i < count; ++i) {
            server = serverList.getServerData(i);
            if (server.ip.toLowerCase(Locale.ROOT).contains("wynncraft")) {
                break;
            }
        }

        if (i >= count) {
            server = new ServerData("Wynncraft", ip, false);
            if (addNew) {
                serverList.addServerData(server);
                serverList.saveServerList();
            }
        }

        return server;
    }

    public static ServerData changeServerIP(ServerData serverData, String newIp, String defaultName) {
        return changeServerIP(new ServerList(Minecraft.getInstance()), serverData, newIp, defaultName);
    }

    /**
     * Change the ip of a ServerData and save the results
     *
     * @param list The server list
     * @param serverData The old server data
     * @param newIp The ip to change to
     * @param defaultName The name to set to if the server data is not found
     * @return The newly saved ServerData
     */
    public static ServerData changeServerIP(ServerList list, ServerData serverData, String newIp, String defaultName) {
        if (serverData == null) {
            list.addServerData(serverData = new ServerData(defaultName, newIp, false));
            list.saveServerList();
            return serverData;
        }

        for (int i = 0, length = list.countServers(); i < length; ++i) {
            ServerData fromList = list.getServerData(i);
            if (Objects.equals(fromList.ip, serverData.ip) && Objects.equals(fromList.serverName, serverData.serverName)) {
                // Found the server data; Replace the ip
                fromList.ip = newIp;
                list.saveServerList();
                return fromList;
            }
        }

        // Not found
        list.addServerData(serverData = new ServerData(defaultName, newIp, false));
        list.saveServerList();
        return serverData;
    }

}
