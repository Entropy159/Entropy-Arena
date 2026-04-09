package com.entropy.arena.api;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameRules;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ArenaUtils {
    public static <T> ListTag listToTag(List<T> list, Function<T, Tag> valueFunction) {
        ListTag tag = new ListTag();
        list.forEach(obj -> tag.add(valueFunction.apply(obj)));
        return tag;
    }

    public static <T> ArrayList<T> tagToArrayList(ListTag tag, Function<Tag, T> converter) {
        ArrayList<T> list = new ArrayList<>();
        if (tag != null) tag.forEach(t -> list.add(converter.apply(t)));
        return list;
    }

    public static <T, P> CompoundTag mapToTag(java.util.Map<T, P> map, Function<T, String> keyFunction, Function<P, Tag> valueFunction) {
        CompoundTag tag = new CompoundTag();
        map.forEach((key, value) -> tag.put(keyFunction.apply(key), valueFunction.apply(value)));
        return tag;
    }

    public static <T, P> HashMap<T, P> tagToHashMap(CompoundTag tag, Function<String, T> keyConverter, Function<Tag, P> valueConverter) {
        HashMap<T, P> map = new HashMap<>();
        if (tag != null)
            tag.getAllKeys().forEach(key -> map.put(keyConverter.apply(key), valueConverter.apply(tag.get(key))));
        return map;
    }

    public static String toTitleCase(String input) {
        return Arrays.stream(input.toLowerCase().split("_")).filter(s -> !s.isEmpty()).map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(Collectors.joining(" "));
    }

    public static void teleportToPos(ServerPlayer player, BlockPos pos) {
        player.teleportTo(pos.getBottomCenter().x, pos.getBottomCenter().y, pos.getBottomCenter().z);
    }

    public static float lerp(float a, float b, float f) {
        return (float) (a * (1.0 - f)) + (b * f);
    }

    public static int lerpColors(int one, int two, float factor) {
        Color color1 = new Color(one);
        Color color2 = new Color(two);
        int red = (int) lerp(color1.getRed(), color2.getRed(), factor);
        int green = (int) lerp(color1.getGreen(), color2.getGreen(), factor);
        int blue = (int) lerp(color1.getBlue(), color2.getBlue(), factor);
        int alpha = (int) lerp(color1.getAlpha(), color2.getAlpha(), factor);
        return new Color(red, green, blue, alpha).getRGB();
    }

    public static void broadcastToOps(MinecraftServer server, @Nullable ServerPlayer origin, Component message) {
        Component messageWithPrefix = (origin == null ? message.copy() : Component.translatable("chat.type.admin", origin.getDisplayName(), message)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        if (server.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
            server.getPlayerList().getPlayers().forEach(player -> {
                if (player != origin && server.getPlayerList().isOp(player.getGameProfile())) {
                    player.sendSystemMessage(messageWithPrefix);
                }
            });
        }

        if (server.getGameRules().getBoolean(GameRules.RULE_LOGADMINCOMMANDS)) {
            server.sendSystemMessage(messageWithPrefix);
        }
    }

    public static void playSoundForEveryone(ServerLevel level, SoundEvent event, SoundSource source) {
        level.players().forEach(player -> playSoundForPlayer(level, player, event, source));
    }

    public static void playSoundForPlayer(ServerLevel level, ServerPlayer player, SoundEvent event, SoundSource source) {
        player.connection.send(new ClientboundSoundPacket(Holder.direct(SoundEvent.createFixedRangeEvent(event.getLocation(), 16)), source, player.getEyePosition().x, player.getEyePosition().y, player.getEyePosition().z, 1, 1, level.getRandom().nextLong()));
    }
}
