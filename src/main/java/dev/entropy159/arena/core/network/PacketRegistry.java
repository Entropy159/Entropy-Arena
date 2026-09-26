package dev.entropy159.arena.core.network;

import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.core.network.toClient.*;
import dev.entropy159.arena.core.network.toServer.AdminMenuPacket;
import dev.entropy159.arena.core.network.toServer.OpenLoadoutsPacket;
import dev.entropy159.arena.core.network.toServer.OpenVotingPacket;
import dev.entropy159.arena.core.network.toServer.ScreenshotPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class PacketRegistry {
    @SubscribeEvent
    public static void registerPackets(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(TimerPacket.TYPE, TimerPacket.STREAM_CODEC, TimerPacket::handle);
        registrar.playToClient(RunningPacket.TYPE, RunningPacket.STREAM_CODEC, RunningPacket::handle);
        registrar.playToClient(TakeScreenshotPacket.TYPE, TakeScreenshotPacket.STREAM_CODEC, TakeScreenshotPacket::handle);
        registrar.playToClient(NotificationPacket.TYPE, NotificationPacket.STREAM_CODEC, NotificationPacket::handle);
        registrar.playToClient(ScoresPacket.TYPE, ScoresPacket.STREAM_CODEC, ScoresPacket::handle);
        registrar.playToClient(GameInfoPacket.TYPE, GameInfoPacket.STREAM_CODEC, GameInfoPacket::handle);
        registrar.playToClient(RespawnPacket.TYPE, RespawnPacket.STREAM_CODEC, RespawnPacket::handle);
        registrar.playToClient(PingPacket.TYPE, PingPacket.STREAM_CODEC, PingPacket::handle);
        registrar.playToClient(ConfigOverridesPacket.TYPE, ConfigOverridesPacket.STREAM_CODEC, ConfigOverridesPacket::handle);

        registrar.playToServer(ScreenshotPacket.TYPE, ScreenshotPacket.STREAM_CODEC, ScreenshotPacket::handle);
        registrar.playToServer(AdminMenuPacket.TYPE, AdminMenuPacket.STREAM_CODEC, AdminMenuPacket::handle);
        registrar.playToServer(OpenLoadoutsPacket.TYPE, OpenLoadoutsPacket.STREAM_CODEC, OpenLoadoutsPacket::handle);
        registrar.playToServer(OpenVotingPacket.TYPE, OpenVotingPacket.STREAM_CODEC, OpenVotingPacket::handle);

        GamemodeRegistry.forEach(gamemode -> gamemode.registerPacket(registrar));
    }
}
