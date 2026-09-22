package dev.entropy159.arena.api.client;

import dev.entropy159.arena.core.config.ClientConfig;
import dev.entropy159.arena.core.registry.ArenaSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.Music;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MusicControls {
    public static void nextMusic() {
        var manager = Minecraft.getInstance().getMusicManager();
        manager.stopPlaying();
        manager.startPlaying(getMusic());
    }

    public static void tryNextMusic() {
        if (!ClientConfig.CONTINUOUS_MUSIC.get()) {
            nextMusic();
        }
    }

    public static Music getMusic() {
        if (ClientConfig.CONTINUOUS_MUSIC.get()) {
            return ArenaSounds.ARENA_MUSIC;
        }
        if (ClientData.inLobby || !ClientData.running) {
            return ArenaSounds.LOBBY_MUSIC;
        }
        return ArenaSounds.ARENA_MUSIC;
    }
}
