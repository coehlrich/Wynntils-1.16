/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.packet;

import com.wynntils.McIf;
import com.wynntils.core.events.custom.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.IPacket;
import net.minecraftforge.common.MinecraftForge;

public class PacketIncomingFilter extends ChannelInboundHandlerAdapter {

    /**
     * Dispatch a packet incoming event to be checked before reaching the
     * interpreter
     *
     * @see PacketEvent for more information about these events
     *
     *
     * @param ctx The Channel Handler
     * @param msg The incoming IPacket
     * @throws Exception If something fails (idk exactly, that was inherited)
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) return;

        PacketEvent.Incoming<? extends IPacket<?>> event = new PacketEvent.Incoming<>((IPacket<?>) msg, McIf.mc().getConnection(), this, ctx);
        boolean cancel = MinecraftForge.EVENT_BUS.post(event);
        if (cancel) return;

        super.channelRead(ctx, msg);
    }

}
