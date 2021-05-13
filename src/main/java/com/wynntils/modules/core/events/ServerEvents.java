/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.events;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.*;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.entities.EntityManager;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.instances.data.LocationData;
import com.wynntils.core.framework.instances.data.SocialData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.core.instances.packet.PacketIncomingFilter;
import com.wynntils.modules.core.instances.packet.PacketOutgoingFilter;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.core.managers.PacketQueue;
import com.wynntils.modules.core.managers.PartyManager;
import com.wynntils.modules.core.managers.UserManager;
import com.wynntils.modules.core.overlays.UpdateOverlay;
import com.wynntils.modules.core.overlays.ui.ChangelogUI;
import com.wynntils.modules.core.overlays.ui.PlayerInfoReplacer;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.downloader.DownloaderManager;
import com.wynntils.webapi.profiles.TerritoryProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerEvents implements Listener {

    private static final Pattern FRIENDS_LIST = Pattern.compile("^§e(.*)'s? friends \\([0-9]+\\): §r§6(.*)§r$");

    /**
     * Does 5 different things and is triggered when the user joins Wynncraft:
     *  - Register the pipeline that intercepts INCOMING Packets
     *  @see PacketIncomingFilter
     *  - Register the pipline that intercepts OUTGOING Packets
     *  @see PacketOutgoingFilter
     *  - Check if the mod has an update available
     *  - Updates the overlayPlayerList to the Wynntils Version
     *
     * @param e Represents the event
     */
    @SubscribeEvent
    public void joinServer(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        e.getManager().channel().pipeline().addBefore("fml:packet_handler", Reference.MOD_ID + ":packet_filter", new PacketIncomingFilter());
        e.getManager().channel().pipeline().addBefore("fml:packet_handler", Reference.MOD_ID + ":outgoingFilter", new PacketOutgoingFilter());

        GuiIngame ingameGui = Minecraft.getInstance().ingameGUI;
        ReflectionFields.GuiIngame_overlayPlayerList.setValue(ingameGui, new PlayerInfoReplacer(Minecraft.getInstance(), ingameGui));

        WebManager.tryReloadApiUrls(true);
        WebManager.checkForUpdatesOnJoin();

        DownloaderManager.startDownloading();
    }

    boolean waitingForFriendList = false;
    boolean waitingForGuildList = false;

    /**
     * Called when the user joins a Wynncraft World, used to register some stuff:
     *  - Make the player use the command /friend list in order to gatter the user friend list
     *  - Check if the last user class was registered if not, make the player execute /class to register it
     *  - Updates the last class
     *  - Updates and check the Download Queue
     *
     * @param e Represents the event
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void joinWorldEvent(WynnWorldEvent.Join e) {
        if (PlayerInfo.get(CharacterData.class).getClassId() == -1 || CoreDBConfig.INSTANCE.lastClass == ClassType.NONE)
            Minecraft.getInstance().player.chat("/class");

        // This codeblock will only be executed if the Wynncraft AUTOJOIN setting is enabled
        // Reason: When you join a world with autojoin enabled, your current class is NONE,
        // while joining without autojoin will make your current class into the selected over the character selection.
        CharacterData data = PlayerInfo.get(CharacterData.class);
        if (data.getCurrentClass() == ClassType.NONE && CoreDBConfig.INSTANCE.lastClass != ClassType.NONE) {
            data.updatePlayerClass(CoreDBConfig.INSTANCE.lastClass, CoreDBConfig.INSTANCE.lastClassIsReskinned);
        }

        if (Reference.onWars || Reference.onNether) return; // avoid dispatching commands while in wars/nether

        // guild members
        if (WebManager.getPlayerProfile() != null && WebManager.getPlayerProfile().getGuildName() != null) {
            waitingForGuildList = true;
            Minecraft.getInstance().player.chat("/guild list");
        }

        // friends
        waitingForFriendList = true;
        Minecraft.getInstance().player.chat("/friends list");

        // party members
        PartyManager.handlePartyList();  // party list here

        startUpdateRegionName();
    }

    /**
     * Detects and register the current friend list of the user
     * Called when the client receives a chat message
     *
     * Also detects the guild list messages to register the guild members
     *
     * @param e Represents the Event
     */
    @SubscribeEvent
    public void chatMessage(ClientChatReceivedEvent e) {
        if (e.isCanceled() || e.getType() != ChatType.SYSTEM) {
            return;
        }
        PartyManager.handleMessages(e.getMessage());  // party messages here

        String messageText = e.getMessage().getUnformattedText();
        String formatted = e.getMessage().getFormattedText();
        Matcher m = FRIENDS_LIST.matcher(formatted);
        if (m.find() && m.group(1).equals(Minecraft.getInstance().player.getName())) {
            String[] friends = m.group(2).split(", ");

            Set<String> friendsList = PlayerInfo.get(SocialData.class).getFriendList();
            Set<String> newFriendsList = new HashSet<>(Arrays.asList(friends));
            PlayerInfo.get(SocialData.class).setFriendList(newFriendsList);

            FrameworkManager.getEventBus().post(new WynnSocialEvent.FriendList.Remove(Sets.difference(friendsList, newFriendsList), false));
            FrameworkManager.getEventBus().post(new WynnSocialEvent.FriendList.Add(Sets.difference(newFriendsList, friendsList), false));

            if (waitingForFriendList) e.setCanceled(true);
            waitingForFriendList = false;
            return;
        }

        // If you don't have any friends, we get a two line response. Hide both.
        if (waitingForFriendList && formatted.equals("§eWe couldn't find any friends.§r")) {
            e.setCanceled(true);
        }
        if (waitingForFriendList && formatted.equals("§eTry typing §r§6/friend add Username§r§e!§r")) {
            waitingForFriendList = false;
            e.setCanceled(true);
        }
        if (waitingForGuildList && formatted.equals("§cYou are not in a guild.§r")) {
            waitingForGuildList = false;
            e.setCanceled(true);
        }
        if (messageText.startsWith("#") && messageText.contains(" XP -")) {
            if (waitingForGuildList) e.setCanceled(true);

            String[] splitMessage = messageText.split(" ");
            if (PlayerInfo.get(SocialData.class).addGuildMember(splitMessage[1])) {
                FrameworkManager.getEventBus().post(new WynnSocialEvent.Guild.Join(splitMessage[1]));
            }
            return;
        }
        if (!messageText.startsWith("[") && messageText.contains("guild") && messageText.contains(" ")) {
            String[] splittedText = messageText.split(" ");
            if (!splittedText[1].equalsIgnoreCase("has")) return;

            if (splittedText[2].equalsIgnoreCase("joined")) {
                if (PlayerInfo.get(SocialData.class).addGuildMember(splittedText[0])) {
                    FrameworkManager.getEventBus().post(new WynnSocialEvent.Guild.Join(splittedText[0]));
                }
                return;
            }

            if (splittedText[2].equalsIgnoreCase("kicked")) {
                if (PlayerInfo.get(SocialData.class).removeGuildMember(splittedText[3])) {
                    FrameworkManager.getEventBus().post(new WynnSocialEvent.Guild.Leave(splittedText[3]));
                }
            }
        }
    }

    /**
     * Detects if the user added or removed a user from their friend list
     * Called when the user execute /friend add or /friend remove
     *
     * Also detects the guild list command used to parse the entire guild list
     *
     * @param e Represents the Event
     */
    @SubscribeEvent
    public void addFriend(ClientChatEvent e) {
        if (e.getMessage().startsWith("/friend add ")) {
            String addedFriend = e.getMessage().replace("/friend add ", "");
            if (PlayerInfo.get(SocialData.class).addFriend(addedFriend)) {
                FrameworkManager.getEventBus().post(new WynnSocialEvent.FriendList.Add(Collections.singleton(addedFriend), true));
            }
        } else if (e.getMessage().startsWith("/friend remove ")) {
            String removedFriend = e.getMessage().replace("/friend remove ", "");
            if (PlayerInfo.get(SocialData.class).removeFriend(removedFriend)) {
                FrameworkManager.getEventBus().post(new WynnSocialEvent.FriendList.Remove(Collections.singleton(removedFriend), true));
            }
        } else if (e.getMessage().startsWith("/guild list") || e.getMessage().startsWith("/gu list")) {
            waitingForGuildList = false;
        }
    }

    /**
     * Verifies the response from packet queue
     */
    @SubscribeEvent
    public void onReceivePacket(PacketEvent e) {
        PacketQueue.checkResponse(e.getPacket());
    }

    /**
     * Warns user if Athena is currently down
     */
    @SubscribeEvent
    public void onJoinServer(WynncraftServerEvent.Login e) {
        if (WebManager.isAthenaOnline()) return;

        StringTextComponent msg = new StringTextComponent("The Wynntils servers are currently down! You can still use Wynntils, but some features may not work. Our servers should be back soon.");
        msg.getStyle().setColor(TextFormatting.RED);
        msg.getStyle().setBold(true);
        new Delay(() -> Minecraft.getInstance().player.sendMessage(msg), 30); // delay so the player actually loads in
    }

    private static boolean triedToShowChangelog = false;

    /**
     * Detects when the user enters the Wynncraft Server
     * Used for displaying the Changelog UI
     */
    @SubscribeEvent
    public void onJoinLobby(WynnClassChangeEvent e) {
        if (!Reference.onServer || !CoreDBConfig.INSTANCE.enableChangelogOnUpdate || !CoreDBConfig.INSTANCE.showChangelogs) return;
        if (UpdateOverlay.isDownloading() || DownloaderManager.isRestartOnQueueFinish() || Minecraft.getInstance().level == null) return;
        if (e.getNewClass() == ClassType.NONE) return;

        synchronized (this) {
            if (triedToShowChangelog) return;
            triedToShowChangelog = true;
        }

        boolean major = !CoreDBConfig.INSTANCE.lastVersion.equals(Reference.VERSION) || CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE;
        new Thread(() -> {
            List<String> changelog = WebManager.getChangelog(major, false);
            if (changelog == null) return;

            Minecraft.getInstance().submit(() -> {
                Minecraft.getInstance().displayGuiScreen(new ChangelogUI(changelog, major));

                // Showed changelog; Don't show next time.
                CoreDBConfig.INSTANCE.showChangelogs = false;
                CoreDBConfig.INSTANCE.saveSettings(CoreModule.getModule());
            });
        }, "wynntils-changelog-downloader").start();
    }

    static BlockPos currentSpawn = null;

    /**
     *  Block compass changing locations if a forced location is set
     */
    @SubscribeEvent
    public void onCompassChange(PacketEvent<SWorldSpawnChangedPacket> e) {
        currentSpawn = e.getPacket().getPos();
        if (Minecraft.getInstance().player == null) {
            CompassManager.reset();
            return;
        }

        if (CompassManager.getCompassLocation() != null) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void leaveServer(WynncraftServerEvent.Leave e) {
        UserManager.clearRegistry();
        EntityManager.clearEntities();

        if (updateTimer != null && !updateTimer.isCancelled()) {
            updateTimer.cancel(true);
        }
    }

    public static BlockPos getCurrentSpawnPosition() {
        return currentSpawn;
    }

    @SubscribeEvent
    public void worldLeave(WynnWorldEvent.Leave e) {
        if (updateTimer != null) {
            updateTimer.cancel(true);
        }
    }

    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("wynntils-location-updater").build());
    private static ScheduledFuture updateTimer;

    /**
     * Starts to check player location for current player territory info
     */
    private static void startUpdateRegionName() {
        updateTimer = executor.scheduleAtFixedRate(() -> {
            ClientPlayerEntity pl = ModCore.mc().player;

            FrameworkManager.getEventBus().post(new SchedulerEvent.RegionUpdate());

            LocationData data = PlayerInfo.get(LocationData.class);
            if (!data.inUnknownLocation()) {
                String location = data.getLocation();
                if (!WebManager.getTerritories().containsKey(location)) {
                    location = location.replace('\'', '’');
                }

                TerritoryProfile currentLocation = WebManager.getTerritories().get(location);

                if (currentLocation != null && currentLocation.insideArea((int) pl.getX(), (int) pl.getZ())) {
                    return;
                }
            }

            for (TerritoryProfile pf : WebManager.getTerritories().values()) {
                if (pf.insideArea((int) pl.getX(), (int) pl.getZ())) {
                    data.setLocation(pf.getFriendlyName());
                    return;
                }
            }

            int chunkX = pl.xChunk; // FIXME: 1.16 -- is this the correct replacement? /magicus
            int chunkZ = pl.zChunk;

            // housing instances are over these chunk coordinates
            if (chunkX >= 4096 && chunkZ >= 4096) {
                if (data.inHousing()) return;

                data.setLocation("Housing");
                return;
            }

            // war arenas are below these chunk coordinates
            if (chunkX <= -4096 && chunkZ <= -4096) {
                if (data.inWars()) return;

                data.setLocation("Wars");
                return;
            }

            // none match, set to unknow
            if (!data.inUnknownLocation()) {
                data.setLocation("");
            }

        }, 0, 3, TimeUnit.SECONDS);
    }

}
