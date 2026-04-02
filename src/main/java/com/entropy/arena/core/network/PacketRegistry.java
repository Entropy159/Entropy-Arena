package com.entropy.arena.core.network;

import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.network.toClient.*;
import com.entropy.arena.core.network.toServer.ScreenshotPacket;
import com.entropy.arena.core.network.toServer.VotePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class PacketRegistry {
    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1").optional();

        registrar.playToClient(TimerPacket.TYPE, TimerPacket.STREAM_CODEC, TimerPacket::handle);
        registrar.playToClient(RunningPacket.TYPE, RunningPacket.STREAM_CODEC, RunningPacket::handle);
        registrar.playToClient(TakeScreenshotPacket.TYPE, TakeScreenshotPacket.STREAM_CODEC, TakeScreenshotPacket::handle);
        registrar.playToClient(VotableMapsPacket.TYPE, VotableMapsPacket.STREAM_CODEC, VotableMapsPacket::handle);
        registrar.playToClient(NotificationPacket.TYPE, NotificationPacket.STREAM_CODEC, NotificationPacket::handle);
        registrar.playToClient(ScoresPacket.TYPE, ScoresPacket.STREAM_CODEC, ScoresPacket::handle);
        registrar.playToClient(GameInfoPacket.TYPE, GameInfoPacket.STREAM_CODEC, GameInfoPacket::handle);

        registrar.playToServer(ScreenshotPacket.TYPE, ScreenshotPacket.STREAM_CODEC, ScreenshotPacket::handle);
        registrar.playToServer(VotePacket.TYPE, VotePacket.STREAM_CODEC, VotePacket::handle);

        GamemodeRegistry.forEach(gamemode -> gamemode.registerPacket(registrar));
    }
}
