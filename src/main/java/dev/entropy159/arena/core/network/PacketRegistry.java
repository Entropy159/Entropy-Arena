package dev.entropy159.arena.core.network;

import dev.entropy159.arena.api.gamemode.GamemodeRegistry;
import dev.entropy159.arena.core.network.toClient.*;
import dev.entropy159.arena.core.network.toServer.*;
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
        registrar.playToClient(VotableMapsPacket.TYPE, VotableMapsPacket.STREAM_CODEC, VotableMapsPacket::handle);
        registrar.playToClient(NotificationPacket.TYPE, NotificationPacket.STREAM_CODEC, NotificationPacket::handle);
        registrar.playToClient(ScoresPacket.TYPE, ScoresPacket.STREAM_CODEC, ScoresPacket::handle);
        registrar.playToClient(GameInfoPacket.TYPE, GameInfoPacket.STREAM_CODEC, GameInfoPacket::handle);
        registrar.playToClient(LoadoutsPacket.TYPE, LoadoutsPacket.STREAM_CODEC, LoadoutsPacket::handle);
        registrar.playToClient(RespawnPacket.TYPE, RespawnPacket.STREAM_CODEC, RespawnPacket::handle);
        registrar.playToClient(PingPacket.TYPE, PingPacket.STREAM_CODEC, PingPacket::handle);
        registrar.playToClient(ConfigOverridesPacket.TYPE, ConfigOverridesPacket.STREAM_CODEC, ConfigOverridesPacket::handle);

        registrar.playToServer(ScreenshotPacket.TYPE, ScreenshotPacket.STREAM_CODEC, ScreenshotPacket::handle);
        registrar.playToServer(MapVotePacket.TYPE, MapVotePacket.STREAM_CODEC, MapVotePacket::handle);
        registrar.playToServer(TypeVotePacket.TYPE, TypeVotePacket.STREAM_CODEC, TypeVotePacket::handle);
        registrar.playToServer(LoadoutSelectPacket.TYPE, LoadoutSelectPacket.STREAM_CODEC, LoadoutSelectPacket::handle);
        registrar.playToServer(AdminMenuPacket.TYPE, AdminMenuPacket.STREAM_CODEC, AdminMenuPacket::handle);
        registrar.playToServer(UpdateMapPacket.TYPE, UpdateMapPacket.STREAM_CODEC, UpdateMapPacket::handle);
        registrar.playToServer(UpdateLoadoutPacket.TYPE, UpdateLoadoutPacket.STREAM_CODEC, UpdateLoadoutPacket::handle);
        registrar.playToServer(LoadoutEditPacket.TYPE, LoadoutEditPacket.STREAM_CODEC, LoadoutEditPacket::handle);

        GamemodeRegistry.forEach(gamemode -> gamemode.registerPacket(registrar));
    }
}
