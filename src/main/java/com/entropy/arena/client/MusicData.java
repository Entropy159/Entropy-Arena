package com.entropy.arena.client;

import com.entropy.arena.core.EntropyArena;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = EntropyArena.MODID, value = Dist.CLIENT)
public class MusicData {
    public static int minDelay = 2;
    public static int maxDelay = 5;
    public static ResourceLocation musicSelected;
    public static ResourceLocation currentMusicLocation = ResourceLocation.withDefaultNamespace("current");
    public static boolean init = true;
    public static boolean inCustomTracking = false;
    public static boolean isPaused = false;
    public static boolean shouldPlay = true;
    public static boolean nextMusic = false;
    public static boolean categoryChanged = false;
    public static String currentCategory;

    public static final Lazy<KeyMapping> NEXT_MUSIC = Lazy.of(() -> new KeyMapping("key.next_music", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, "key.categories." + EntropyArena.MODID));

    @SubscribeEvent
    public static void keybinds(RegisterKeyMappingsEvent event) {
        event.register(NEXT_MUSIC.get());
    }

    @SubscribeEvent
    public static void postRenderTick(ClientTickEvent.Post event) {
        while (NEXT_MUSIC.get().consumeClick()) {
            MusicData.nextMusic = true;
        }
    }
}