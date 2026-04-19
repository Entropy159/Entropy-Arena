package com.entropy.arena.api;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;

public enum ArenaGameType implements StringRepresentable {
    TIMED(true),
    SCORE(false);

    public static final Codec<ArenaGameType> CODEC = StringRepresentable.fromEnum(ArenaGameType::values);
    private static final IntFunction<ArenaGameType> BY_ID = ByIdMap.continuous(ArenaGameType::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ArenaGameType> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ArenaGameType::ordinal);

    private final boolean timed;

    ArenaGameType(boolean timed) {
        this.timed = timed;
    }

    public boolean isTimed() {
        return timed;
    }

    public MutableComponent getName() {
        return Component.translatable("arena.type." + getSerializedName());
    }

    public MutableComponent getVotedComponent() {
        return Component.translatable("arena.message.voted_for_type", getName());
    }

    public MutableComponent getVotesComponent(int votes) {
        return getName().append(": " + votes);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }
}
