/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules;


import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.questbook.QuestBookModule;

public class ModuleRegistry {

    /**
     * This registers all modules that should be loaded
     */
    public static void registerModules() {
        FrameworkManager.registerModule(new QuestBookModule());
        FrameworkManager.registerModule(new CoreModule());
//        FrameworkManager.registerModule(new UtilitiesModule());
//        FrameworkManager.registerModule(new RichPresenceModule());
//        FrameworkManager.registerModule(new CosmeticsModule());
//        FrameworkManager.registerModule(new MusicModule());
//        FrameworkManager.registerModule(new ChatModule());
//        FrameworkManager.registerModule(new MapModule());
//        FrameworkManager.registerModule(new VisualModule());
    }

}
