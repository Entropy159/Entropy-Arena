package com.entropy.arena.core.network.toClient;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.entropy.arena.api.client.ClientData;
import com.entropy.arena.core.EntropyArena;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ConfigOverridesPacket(
        Map<String, CommentedConfig> overrides) implements CustomPacketPayload {
    public static final Type<ConfigOverridesPacket> TYPE = new Type<>(EntropyArena.id("config_overrides"));
    public static final StreamCodec<FriendlyByteBuf, ConfigOverridesPacket> STREAM_CODEC = StreamCodec.of((buf, val) -> {
        buf.writeInt(val.overrides.size());
        val.overrides.forEach((modID, config) -> {
            buf.writeUtf(modID);
            buf.writeUtf(new TomlWriter().writeToString(config));
        });
    }, buf -> {
        int size = buf.readInt();
        Map<String, CommentedConfig> overrides = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String modID = buf.readUtf();
            String data = buf.readUtf();
            overrides.put(modID, new TomlParser().parse(data));
        }
        return new ConfigOverridesPacket(overrides);
    });

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.configOverrides = overrides;
    }
}
