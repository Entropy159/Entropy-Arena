package com.entropy.arena.api.map;

import com.entropy.arena.core.EntropyArena;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MapScreenshot {
    public static final StreamCodec<ByteBuf, MapScreenshot> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, MapScreenshot::getMapName, ByteBufCodecs.BYTE_ARRAY, MapScreenshot::getData, MapScreenshot::new);
    private static final int screenshotWidth = 256;

    private final String mapName;
    private final byte[] data;
    private ResourceLocation textureId;
    @OnlyIn(Dist.CLIENT)
    private DynamicTexture texture;
    private float aspectRatio = 1;

    public MapScreenshot(String mapName) {
        this(mapName, new byte[0]);
    }

    public MapScreenshot(String mapName, byte[] data) {
        this.mapName = mapName;
        this.data = data;
    }

    public String getMapName() {
        return mapName;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isPresent() {
        return data.length > 0;
    }

    @OnlyIn(Dist.CLIENT)
    public static MapScreenshot takeScreenshot(String mapName) {
        try (NativeImage screenshot = Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget())) {
            int newWidth = Math.min(MapScreenshot.screenshotWidth, screenshot.getWidth());
            try (NativeImage scaled = downscale(screenshot, newWidth)) {
                return new MapScreenshot(mapName, scaled.asByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static NativeImage downscale(NativeImage src, int targetWidth) {
        int targetHeight = (int) ((float) src.getHeight() / src.getWidth() * targetWidth);
        NativeImage dst = new NativeImage(targetWidth, targetHeight, false);
        src.resizeSubRectTo(0, 0, src.getWidth(), src.getHeight(), dst);
        return dst;
    }

    @OnlyIn(Dist.CLIENT)
    public void render(GuiGraphics graphics, int x, int y, int width) {
        if (textureId != null) {
            int height = (int) (width * aspectRatio);
            graphics.blit(textureId, x, y, 0, 0, width, height, width, height);
        } else {
            bindTexture();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getAspectRatio() {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            try (NativeImage image = NativeImage.read(input)) {
                return (float) image.getHeight() / image.getWidth();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void bindTexture() {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            RenderSystem.recordRenderCall(() -> {
                try (NativeImage image = NativeImage.read(input)) {
                    aspectRatio = (float) image.getHeight() / image.getWidth();
                    if (texture != null) {
                        texture.close();
                    }
                    texture = new DynamicTexture(image);
                    textureId = EntropyArena.id("map_" + mapName.toLowerCase().replace(" ", "_"));
                    Minecraft.getInstance().getTextureManager().register(textureId, texture);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void clear() {
        if (texture != null) {
            texture.close();
            texture = null;
            textureId = null;
        }
    }
}
