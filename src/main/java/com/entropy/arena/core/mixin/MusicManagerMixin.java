package com.entropy.arena.core.mixin;

import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.core.registry.ArenaSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.entropy.arena.client.MusicData.*;

@Mixin(MusicManager.class)
public abstract class MusicManagerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private RandomSource random;
    @Shadow
    private int nextSongDelay;
    @Shadow
    private SoundInstance currentMusic;
    @Unique
    private boolean spellbookArena$displayPrompted = false;

    @Shadow
    public abstract void startPlaying(net.minecraft.sounds.Music var1);

    @Inject(method = {"startPlaying(Lnet/minecraft/sounds/Music;)V"}, at = {@At("HEAD")}, cancellable = true)
    private void playMusic(net.minecraft.sounds.Music type, CallbackInfo ci) {
        inCustomTracking = false;
        if (init && this.minecraft.level != null) {
            if (!shouldPlay) {
                shouldPlay = true;
            } else {
                this.minecraft.getSoundManager().stop(this.currentMusic);
                if (musicSelected != null) {
                    currentMusicLocation = musicSelected;
                    musicSelected = null;
                } else {
                    currentMusicLocation = (!ClientData.inLobby && ClientData.running) ? ArenaSounds.ARENA_SOUND.getId() : ArenaSounds.LOBBY_SOUND.getId();
                }

                this.currentMusic = SimpleSoundInstance.forMusic(SoundEvent.createVariableRangeEvent(currentMusicLocation));

                if (this.currentMusic.getSound() != SoundManager.EMPTY_SOUND) {
                    this.minecraft.getSoundManager().play(this.currentMusic);
                    inCustomTracking = true;
                }

                this.spellbookArena$displayMusic();
            }
            this.nextSongDelay = spellbookArena$getTimer(this.random);
            ci.cancel();
        }
    }

    @Inject(method = {"tick()V"}, at = {@At("HEAD")}, cancellable = true)
    private void handleMusic(CallbackInfo ci) {
        this.spellbookArena$handleNextMusicKey();
        if (inCustomTracking) {
            if (this.minecraft != null && init && this.minecraft.level != null && this.currentMusic != null && this.minecraft.getSoundManager().isActive(this.currentMusic)) {
                ci.cancel();
            } else {
                inCustomTracking = false;
            }
        }

        if (isPaused && (this.currentMusic == null || this.minecraft != null && !this.minecraft.getSoundManager().isActive(this.currentMusic))) {
            ++this.nextSongDelay;
        }

    }

    @Unique
    private void spellbookArena$displayMusic() {
        this.spellbookArena$printMusic();

        if (categoryChanged) {
            categoryChanged = false;
        }
    }

    @Unique
    private void spellbookArena$printMusic() {
        if (this.minecraft.level != null) {
            String currentMusic = this.currentMusic != null ? this.currentMusic.getSound().getLocation().toString() : SoundManager.EMPTY_SOUND.getLocation().toString();
            if (currentMusic.equals(SoundManager.EMPTY_SOUND.getLocation().toString())) {
                if (!this.spellbookArena$displayPrompted) {
                    return;
                }

                this.spellbookArena$displayPrompted = false;
                double remaining = (double) this.nextSongDelay / (double) 20.0F;
                spellbookArena$print(this.minecraft, Component.literal("No music playing (Next selection after " + remaining + "s)"));
            } else {
                Component category = Component.translatableWithFallback("music.category." + currentCategory, currentCategory == null ? "" : currentCategory.toUpperCase().replace('_', ' '));
                Component music = Component.translatable(currentMusic);
                Component content = categoryChanged ? Component.literal(category + " / " + music) : music;
                spellbookArena$print(this.minecraft, Component.translatable("record.nowPlaying", content));
            }

        }
    }

    @Unique
    private void spellbookArena$printPaused() {
        spellbookArena$print(this.minecraft, Component.literal("PAUSED"));
    }

    @Unique
    private void spellbookArena$handleNextMusicKey() {
        if (nextMusic) {
            nextMusic = false;
            if (isPaused) {
                this.spellbookArena$printPaused();
            } else {
                this.spellbookArena$displayPrompted = true;
                this.startPlaying(this.minecraft.getSituationalMusic());
            }
        }

    }

    @Unique
    private static int spellbookArena$getTimer(RandomSource random) {
        return Mth.nextInt(random, minDelay * 10, maxDelay * 20);
    }

    @Unique
    private static void spellbookArena$print(Minecraft client, Component text) {
        client.gui.setOverlayMessage(text, true);
    }
}
