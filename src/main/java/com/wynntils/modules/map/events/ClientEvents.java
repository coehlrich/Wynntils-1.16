/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.map.events;

import com.wynntils.McIf;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.*;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.map.instances.LabelBake;
import com.wynntils.modules.core.managers.CompassManager;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.modules.map.instances.WaypointProfile;
import com.wynntils.modules.map.managers.BeaconManager;
import com.wynntils.modules.map.managers.GuildResourceManager;
import com.wynntils.modules.map.managers.LootRunManager;
import com.wynntils.modules.utilities.instances.Toast;
import com.wynntils.modules.utilities.overlays.hud.ToastOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.network.play.server.SAdvancementInfoPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.modules.core.events.ClientEvents.*;

public class ClientEvents implements Listener {
    private static final Pattern MOB_LABEL = Pattern.compile("^.*\\[Lv. [0-9]+\\]$");
    private static final Pattern HEALTH_LABEL = Pattern.compile("^\\[\\|+[0-9]+\\|+\\]$");
    private static final Pattern TOTEM_LABEL = Pattern.compile("^§c[0-9]+s|\\§c+[0-9]+❤/§7s$");
    private static final Pattern GATHERING_LABEL = Pattern.compile("^. [ⒸⒷⒿⓀ] .* Lv. Min: [0-9]+$");
    private static final Pattern RESOURCE_LABEL = Pattern.compile("^(?:Right|Left)-Click for .*$");
    private static final Pattern WYBEL_OWNER = Pattern.compile("^§7\\[.*\\]$");
    private static final Pattern WYBEL_LEVEL = Pattern.compile("^§2Lv. §a[0-9]+.*$");

    BlockPos lastLocation = null;

    @SubscribeEvent
    public void renderWorld(RenderWorldLastEvent e) {
        LootRunManager.renderActivePaths();

        if (!MapConfig.INSTANCE.showCompassBeam || CompassManager.getCompassLocation() == null) return;

        Location compass = CompassManager.getCompassLocation();
        BeaconManager.drawBeam(new Location(compass.getX(), compass.getY(), compass.getZ()), MapConfig.INSTANCE.compassBeaconColor, e.getPartialTicks());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void openChest(PlayerInteractEvent.RightClickBlock e) {
        if (e.getPos() == null || e.isCanceled()) return;
        BlockPos pos = e.getPos();
        BlockState state = e.getPlayer().level.getBlockState(pos);
        if (!(state.getBlock() instanceof ContainerBlock)) return;
        lastLocation = pos.immutable();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void guiOpen(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (lastLocation == null) return;
        if (!McIf.toText(e.getGui().getTitle()).contains("Loot Chest")) {
            lastLocation = null;
            return;
        }

        if (LootRunManager.isRecording())
            LootRunManager.addChest(lastLocation); // add chest to the current lootrun recording

        String tier = McIf.toText(e.getGui().getTitle()).replace("Loot Chest ", "");
        if (!MapConfig.Waypoints.INSTANCE.chestTiers.isTierAboveThis(tier)) return;

        WaypointProfile wp = null;
        switch (tier) {
            case "IV":
                wp = new WaypointProfile("Loot Chest T4", lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), CommonColors.WHITE, WaypointProfile.WaypointType.LOOTCHEST_T4, -1000);
                wp.setGroup(WaypointProfile.WaypointType.LOOTCHEST_T4);
                break;
            case "III":
                wp = new WaypointProfile("Loot Chest T3", lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), CommonColors.WHITE, WaypointProfile.WaypointType.LOOTCHEST_T3, -1000);
                wp.setGroup(WaypointProfile.WaypointType.LOOTCHEST_T3);
                break;
            case "II":
                wp = new WaypointProfile("Loot Chest T2", lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), CommonColors.WHITE, WaypointProfile.WaypointType.LOOTCHEST_T2, -1000);
                wp.setGroup(WaypointProfile.WaypointType.LOOTCHEST_T2);
                break;
            case "I":
                wp = new WaypointProfile("Loot Chest T1", lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), CommonColors.WHITE, WaypointProfile.WaypointType.LOOTCHEST_T1, -1000);
                wp.setGroup(WaypointProfile.WaypointType.LOOTCHEST_T1);
                break;
        }
        if (wp != null) {
            if (MapConfig.Waypoints.INSTANCE.waypoints.stream().anyMatch(c -> c.getX() == lastLocation.getX() && c.getY() == lastLocation.getY() && c.getZ() == lastLocation.getZ())) return;

            MapConfig.Waypoints.INSTANCE.waypoints.add(wp);
            MapConfig.Waypoints.INSTANCE.saveSettings(MapModule.getModule());

            ToastOverlay.addToast(new Toast(Toast.ToastType.DISCOVERY, "New Map Entry", "You found a tier " + tier.replace("IV", "4").replace("III", "3") + " chest!"));
        }
    }

    @SubscribeEvent
    public void recordLootRun(TickEvent.ClientTickEvent e) {
        if (!Reference.onWorld || e.phase != TickEvent.Phase.END || !LootRunManager.isRecording()) return;

        ClientPlayerEntity player = McIf.player();
        if (player == null) return;

        Entity lowestEntity = player.getLowestRidingEntity();

        LootRunManager.recordMovement(lowestEntity.getX(), lowestEntity.getY(), lowestEntity.getZ());
    }

    @SubscribeEvent
    public void sendGathering(GameEvent.ResourceGather e) {
        if (!MapConfig.Telemetry.INSTANCE.allowGatheringSpot) return;

        WebManager.getAccount().sendGatheringSpot(e.getType(), e.getMaterial(), e.getLocation());
    }

    @SubscribeEvent
    public void receiveAdvancements(PacketEvent.Incoming<SAdvancementInfoPacket> event) {
        // can be done async without problems
        GuildResourceManager.processAdvancements(event.getPacket());
    }

    @SubscribeEvent
    public void labelDetection(LocationEvent.LabelFoundEvent event) {
        if (!MapConfig.Telemetry.INSTANCE.enableLocationDetection) return;

        String formattedLabel = event.getLabel();
        String label = McIf.getTextWithoutFormattingCodes(formattedLabel);
        Location location = event.getLocation();

        Matcher m = MOB_LABEL.matcher(label);
        if (m.find()) return;

        Matcher m2 = HEALTH_LABEL.matcher(label);
        if (m2.find()) return;

        Matcher m3 = MOB_DAMAGE.matcher(label);
        if (m3.find()) return;

        Matcher m4 = GATHERING_STATUS.matcher(label);
        if (m4.find()) return;

        Matcher m5 = GATHERING_RESOURCE.matcher(label);
        if (m5.find()) return;

        Matcher m6 = TOTEM_LABEL.matcher(formattedLabel);
        if (m6.find()) return;

        Matcher m7 = GATHERING_LABEL.matcher(label);
        if (m7.find()) return;

        Matcher m8 = RESOURCE_LABEL.matcher(label);
        if (m8.find()) return;

        Matcher m9 = WYBEL_OWNER.matcher(formattedLabel);
        if (m9.find()) return;

        Matcher m10 = WYBEL_LEVEL.matcher(formattedLabel);
        if (m10.find()) return;


        LabelBake.handleLabel(label, event.getLabel(), location);
    }

    @SubscribeEvent
    public void labelDetectEntity(LocationEvent.EntityLabelFoundEvent event) {
        if (!MapConfig.Telemetry.INSTANCE.enableLocationDetection) return;

        String name = McIf.getTextWithoutFormattingCodes(event.getLabel());
        Location location = event.getLocation();
        Entity entity = event.getEntity();

        Matcher m = MOB_LABEL.matcher(name);
        if (m.find()) return;

        Matcher m2 = HEALTH_LABEL.matcher(name);
        if (m2.find()) return;

        if (!(entity instanceof VillagerEntity)) return;

        LabelBake.handleNpc(name, event.getLabel(), location);
    }

    @SubscribeEvent
    public void onWorldJoin(WynnWorldEvent.Join e) {
        LabelBake.onWorldJoin(e);
    }

}
