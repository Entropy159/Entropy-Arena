package dev.entropy159.arena.api.map;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.entropy159.arena.core.EntropyArena;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLEnvironment;

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

    public float getAspectRatio() {
        if (FMLEnvironment.dist.isClient()) {
            return aspectRatio();
        }
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    private float aspectRatio() {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            try (NativeImage image = NativeImage.read(input)) {
                return (float) image.getWidth() / image.getHeight();
            }
        } catch (IOException e) {
            EntropyArena.LOGGER.error("Error getting aspect ratio for map screenshot! ", e);
        }
        return 1;
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

    public ResourceLocation getTexture() {
        if (FMLEnvironment.dist.isClient() && textureId == null) {
            bindTexture();
        }
        return textureId;
    }

    public IGuiTexture getGuiTexture() {
        return com.lowdragmc.lowdraglib2.gui.texture.DynamicTexture.of(() -> {
            var texture = getTexture();
            return texture == null ? IGuiTexture.EMPTY : SpriteTexture.of(texture);
        });
    }

    @OnlyIn(Dist.CLIENT)
    private void bindTexture() {
        try (ByteArrayInputStream input = new ByteArrayInputStream(data)) {
            RenderSystem.recordRenderCall(() -> {
                try (NativeImage image = NativeImage.read(input)) {
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
