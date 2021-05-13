/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.inventory;

import com.wynntils.modules.core.interfaces.IInventoryOpenAction;
import com.wynntils.modules.core.managers.PacketQueue;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SOpenWindowPacket;

public class InventoryOpenByCommand implements IInventoryOpenAction {

    String inputCommand;

    public InventoryOpenByCommand(String inputCommand) {
        this.inputCommand = inputCommand;
    }

    @Override
    public void onOpen(FakeInventory inv, Runnable onDrop) {
        PacketQueue.queueComplexPacket(new CChatMessagePacket(inputCommand), SOpenWindowPacket.class).onDrop(onDrop);
    }

}
