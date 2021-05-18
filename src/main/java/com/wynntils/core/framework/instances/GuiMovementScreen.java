/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.instances;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;

public class GuiMovementScreen extends Screen {
    protected GuiMovementScreen(ITextComponent p_i51108_1_) {
        super(p_i51108_1_);
    }

    protected boolean allowMovement = true;

    // TODO: uncomment
//    @Override
//    public void keyPressed() throws IOException {
//
//        if (Keyboard.isCreated()) {
//            while (Keyboard.next()) {
//
//                for (KeyBinding key : McIf.mc().options.keyBindings) {
//                    if (key.getKeyCode() != Keyboard.getEventKey() || key.getKeyConflictContext() != WynntilsConflictContext.ALLOW_MOVEMENTS) continue;
//
//                    KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
//                    KeyBinding.onTick(Keyboard.getEventKey());
//                    return;
//                }
//
//                this.handleKeyboardInput();
//            }
//        }
//    }

}
