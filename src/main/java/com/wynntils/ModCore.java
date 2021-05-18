/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils;

import com.wynntils.core.CoreManager;
import com.wynntils.core.events.custom.ClientEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.rendering.WynnRenderItem;
import com.wynntils.modules.ModuleRegistry;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.modules.map.MapModule;
import com.wynntils.modules.map.configs.MapConfig;
import com.wynntils.webapi.WebManager;
import net.minecraftforge.fml.CrashReportExtender;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.ICrashCallable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod(Reference.MOD_ID)
public class ModCore {

    public static File jarFile = null;
    
    public ModCore() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(null);
	}
    
    public void setup(FMLClientSetupEvent e) {
    	IModInfo info = ModLoadingContext.get().getActiveContainer().getModInfo();
    	Reference.VERSION = info.getVersion().toString();
        String[] splitDescription = info.getDescription().split(" ");
        try {
            Reference.BUILD_NUMBER = Integer.parseInt(splitDescription[splitDescription.length - 1]);
        } catch (NumberFormatException ignored) {}

        jarFile = ((ModFileInfo) info).getFile().getFilePath().toFile();
        Reference.developmentEnvironment = !FMLLoader.isProduction()
                || (System.getProperty("wynntils.development") != null && System.getProperty("wynntils.development").equals("true"));
        // Reference.developmentEnvironment = false;  // Uncomment to test updater

        if (Reference.developmentEnvironment)
            Reference.LOGGER.info("Development environment detected, automatic update detection disabled");

        WebManager.setupUserAccount();
        WebManager.setupWebApi(true);

        CoreManager.preModules();

        ModuleRegistry.registerModules();
        FrameworkManager.startModules();

        CoreManager.afterModules();
        
        Map<String, String> conflicts = new HashMap<>();
        for (ModInfo mod : ModList.get().getMods()) {
            if (!mod.getModId().equalsIgnoreCase("labymod")) continue;

            conflicts.put(mod.getDisplayName(), mod.getVersion().toString());
        }

        // TODO: uncomment
//        if (!conflicts.isEmpty()) throw new ModConflictScreen(conflicts);

        WynnRenderItem.inject();

        FrameworkManager.postEnableModules();

        // TODO: uncomment
        // HeyZeer0: This will reload our cache if a texture or similar is applied
        // This also immediately loads it
//        McIf.mc().getResourceManager().r
//        ((SimpleReloadableResourceManager) McIf.mc().getResourceManager()).registerReloadListener(new ReloadListener<T>() {
//
//            @Override
//            protected Triple<List<>, M, R> prepare(IResourceManager p_212854_1_, IProfiler p_212854_2_) {
//                // TODO Auto-generated method stub
//                return null;
//            }
//
//            @Override
//            protected void apply(T p_212853_1_, IResourceManager p_212853_2_, IProfiler p_212853_3_) {
//                // TODO Auto-generated method stub
//                
//            }
//        };
//            CompletableFuture.allOf()
//            Textures.loadTextures();
//            Mappings.loadMappings();
//        MapApiIcon.resetApiMarkers();
//        });

        if (MapConfig.INSTANCE.enabledMapIcons.containsKey("tnt")) {
            MapConfig.INSTANCE.enabledMapIcons = MapConfig.resetMapIcons(false);
            MapConfig.INSTANCE.saveSettings(MapModule.getModule());
        }

        CrashReportExtender.registerCrashCallable(new ICrashCallable() {
            @Override
            public String getLabel() {
                return "Wynntils Details";
            }

            @Override
            public String call() {
                UpdateStream stream = CoreDBConfig.INSTANCE == null ? null : CoreDBConfig.INSTANCE.updateStream;
                return "Running Wynntils v" + Reference.VERSION + " in " + stream + ", " + (Reference.developmentEnvironment ? "being a dev env" : "at a normal env") + (Reference.onBeta ? " (This crash occured on the Hero Beta)" : "");
            }
        });

        FrameworkManager.getEventBus().post(new ClientEvent.Ready());
    }

}
