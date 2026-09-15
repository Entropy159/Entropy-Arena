package dev.entropy159.arena.api.client;

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
        Music music = ClientData.inLobby || !ClientData.running ? ArenaSounds.LOBBY_MUSIC : ArenaSounds.ARENA_MUSIC;
        manager.startPlaying(music);
    }
}
