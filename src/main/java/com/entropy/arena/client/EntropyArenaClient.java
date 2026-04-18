package com.entropy.arena.client;

import com.entropy.arena.api.Notification;
import com.entropy.arena.api.client.ArenaRenderingUtils;
import com.entropy.arena.api.client.ScreenAnchorPoint;
import com.entropy.arena.api.events.ModifyGlowColorEvent;
import com.entropy.arena.api.map.MapScreenshot;
import com.entropy.arena.client.screen.LoadoutScreen;
import com.entropy.arena.client.screen.VotingScreen;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.network.toServer.ScreenshotPacket;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static com.entropy.arena.api.client.ClientData.*;

@Mod(value = EntropyArena.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = EntropyArena.MODID, value = Dist.CLIENT)
public class EntropyArenaClient {
    private static final Minecraft client = Minecraft.getInstance();

    public static String pendingScreenshot;
    private static boolean nextFrameTakeScreenshot = false;

    public static final Lazy<KeyMapping> MAP_VOTING = Lazy.of(() -> new KeyMapping("key.map_voting", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.categories." + EntropyArena.MODID));
    public static final Lazy<KeyMapping> LOADOUTS = Lazy.of(() -> new KeyMapping("key.loadouts", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_L, "key.categories." + EntropyArena.MODID));

    public EntropyArenaClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void keybinds(RegisterKeyMappingsEvent event) {
        event.register(MAP_VOTING.get());
        event.register(LOADOUTS.get());
    }

    @SubscribeEvent
    public static void postRenderTick(ClientTickEvent.Post event) {
        while (MAP_VOTING.get().consumeClick()) {
            if (inLobby && running) {
                openVotingScreen();
            }
        }
        while (LOADOUTS.get().consumeClick()) {
            if (!inLobby && running) {
                openLoadoutScreen();
            }
        }
    }

    @SubscribeEvent
    public static void modifyTooltips(ItemTooltipEvent event) {
        if (event.getItemStack().has(ArenaDataComponents.ITEM_LIST)) {
            event.getToolTip().add(Component.translatable("arena.tooltip.item_list", event.getItemStack().get(ArenaDataComponents.ITEM_LIST)));
        }
    }

    @SubscribeEvent
    public static void spawnProtection(EntityInvulnerabilityCheckEvent event) {
        if (client.level != null && running && (inLobby || client.level.getGameTime() <= lastRespawn + ServerConfig.SPAWN_PROTECTION.get() * 20L)) {
            event.setInvulnerable(true);
        }
    }

    @SubscribeEvent
    public static void hud(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CROSSHAIR, EntropyArena.id("hud"), (graphics, tracker) -> {
            if (client.player == null || client.level == null) return;

            ArenaRenderingUtils.onRenderStart();

            if (client.options.hideGui) return;

            if (running) {
                ArenaRenderingUtils.renderText(graphics, getTimerText(), ScreenAnchorPoint.TOP_LEFT);
                renderScores(graphics);
                if (currentMap != null && currentGamemode != null) {
                    ArenaRenderingUtils.renderText(graphics, Component.literal(currentMap).withStyle(ChatFormatting.AQUA), ScreenAnchorPoint.BOTTOM_LEFT);
                    ArenaRenderingUtils.renderText(graphics, currentGamemode.getName().copy().withStyle(ChatFormatting.YELLOW), ScreenAnchorPoint.BOTTOM_LEFT);
                    currentGamemode.onClientRender(graphics, tracker);
                }
            }

            renderNotifications(graphics);
        });
    }

    private static Component getTimerText() {
        return targetScore > 0 ? Component.translatable("arena.hud.target_score", targetScore) : Component.translatable((inLobby ? "arena.hud.interval" : "arena.hud.timer"), String.format("%02d:%02d", timer / 60, timer % 60));
    }

    private static void renderScores(GuiGraphics graphics) {
        for (Component score : scoreList) {
            ArenaRenderingUtils.renderText(graphics, score, ScreenAnchorPoint.TOP_LEFT);
        }
    }

    private static void renderNotifications(GuiGraphics graphics) {
        Set<Notification> expired = new HashSet<>();
        for (Notification notification : notifications) {
            if (!notification.tryRender(graphics, ScreenAnchorPoint.TOP_RIGHT)) {
                expired.add(notification);
            }
        }
        notifications.removeAll(expired);
    }

    @SubscribeEvent
    public static void modifyEntityColor(ModifyGlowColorEvent event) {
        if (currentGamemode != null) {
            event.setColor(currentGamemode.modifyEntityColor(event.getEntity(), event.getColor()));
        }
    }

    @SubscribeEvent
    public static void screenshot(ClientTickEvent.Post event) {
        if (nextFrameTakeScreenshot) {
            PacketDistributor.sendToServer(new ScreenshotPacket(MapScreenshot.takeScreenshot(pendingScreenshot)));
            client.options.hideGui = false;
            pendingScreenshot = null;
            nextFrameTakeScreenshot = false;
        }
        if (pendingScreenshot != null) {
            nextFrameTakeScreenshot = true;
        }
    }

    @SubscribeEvent
    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        loadIcon();
    }

    private static void loadIcon() {
        ByteBuffer icon = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            icon = loadIconImage(w, h, channels);
            if (icon == null) {
                return;
            }

            try (GLFWImage.Buffer icons = GLFWImage.malloc(1)) {
                GLFWImage iconImage = icons.get(0);
                iconImage.set(w.get(0), h.get(0), icon);

                GLFW.glfwSetWindowIcon(client.getWindow().getWindow(), icons);
            }
        } catch (Exception e) {
            EntropyArena.LOGGER.error("Failed to set window icon", e);
        } finally {
            if (icon != null) {
                STBImage.stbi_image_free(icon);
            }
        }
    }

    private static ByteBuffer loadIconImage(IntBuffer w, IntBuffer h, IntBuffer channels) throws IOException {
        Resource iconResource = client.getResourceManager().getResourceOrThrow(EntropyArena.id("icon.png"));

        try (InputStream stream = iconResource.open()) {
            byte[] iconBytes = stream.readAllBytes();

            ByteBuffer buffer = ByteBuffer.allocateDirect(iconBytes.length).put(iconBytes).flip();
            ByteBuffer icon = STBImage.stbi_load_from_memory(buffer, w, h, channels, 4);

            if (icon == null) {
                EntropyArena.LOGGER.error("Failed to load icon: {}", STBImage.stbi_failure_reason());
            }

            return icon;
        }
    }

    public static void takeScreenshot(String mapName) {
        client.options.hideGui = true;
        pendingScreenshot = mapName;
    }

    public static void openVotingScreen() {
        client.setScreen(new VotingScreen());
    }

    public static void openLoadoutScreen() {
        client.setScreen(new LoadoutScreen());
    }

    public static void sendChatMessage(Component message) {
        if (client.player != null) client.player.sendSystemMessage(message);
    }
}
