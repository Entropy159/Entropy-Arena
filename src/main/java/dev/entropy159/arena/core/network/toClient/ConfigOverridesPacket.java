package dev.entropy159.arena.core.network.toClient;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.core.EntropyArena;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ConfigOverridesPacket(
        Map<String, CommentedConfig> overrides) implements CustomPacketPayload {
    public static final Type<ConfigOverridesPacket> TYPE = new Type<>(EntropyArena.id("config_overrides"));
    public static final StreamCodec<FriendlyByteBuf, Map<String, CommentedConfig>> CONFIG_MAP_STREAM_CODEC = ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, StreamCodec.of((buf, val) -> buf.writeUtf(new TomlWriter().writeToString(val)), buf -> new TomlParser().parse(buf.readUtf())));
    public static final StreamCodec<FriendlyByteBuf, ConfigOverridesPacket> STREAM_CODEC = StreamCodec.composite(CONFIG_MAP_STREAM_CODEC, ConfigOverridesPacket::overrides, ConfigOverridesPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ClientData.configOverrides = overrides;
    }
}
