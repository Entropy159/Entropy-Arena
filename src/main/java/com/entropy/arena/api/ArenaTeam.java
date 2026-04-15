package com.entropy.arena.api;

import com.entropy.arena.core.config.ServerConfig;
import com.entropy.arena.core.registry.ArenaBlocks;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public enum ArenaTeam implements StringRepresentable {
    RED(DyeColor.RED, ChatFormatting.DARK_RED, 0xFFFF0000),
    BLUE(DyeColor.BLUE, ChatFormatting.DARK_BLUE, 0xFF0000FF),
    YELLOW(DyeColor.YELLOW, ChatFormatting.YELLOW, 0xFFFFFF00),
    GREEN(DyeColor.GREEN, ChatFormatting.DARK_GREEN, 0xFF00FF00),
    ORANGE(DyeColor.ORANGE, ChatFormatting.GOLD, 0xFFFF7F00),
    PURPLE(DyeColor.PURPLE, ChatFormatting.DARK_PURPLE, 0xFF9C00FF),
    CYAN(DyeColor.CYAN, ChatFormatting.AQUA, 0xFF00FFFF),
    PINK(DyeColor.PINK, ChatFormatting.LIGHT_PURPLE, 0xFFFF00FF),
    NONE(DyeColor.WHITE, ChatFormatting.WHITE, 0xFFFFFFFF);

    public static final Codec<ArenaTeam> CODEC = StringRepresentable.fromEnum(ArenaTeam::values);
    private static final IntFunction<ArenaTeam> BY_ID = ByIdMap.continuous(ArenaTeam::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, ArenaTeam> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ArenaTeam::ordinal);

    private final DyeColor dyeColor;
    private final ChatFormatting chatFormatting;
    private final int color;

    ArenaTeam(DyeColor dye, ChatFormatting formatting, int hex) {
        dyeColor = dye;
        chatFormatting = formatting;
        color = hex;
    }

    public static @Nullable ArenaTeam getFromStack(@NotNull ItemStack stack) {
        if (stack.has(ArenaDataComponents.TEAM)) {
            return stack.get(ArenaDataComponents.TEAM);
        }
        for (ArenaTeam team : values()) {
            if (ArenaBlocks.TEAM_BLOCKS.get(team).isIn(stack)) {
                return team;
            }
        }
        DyeColor dye = DyeColor.getColor(stack);
        return dye == null ? null : fromDye(dye);
    }

    public static @Nullable ArenaTeam fromDye(DyeColor dye) {
        for (ArenaTeam team : values()) {
            if (team.dyeColor == dye) return team;
        }
        return null;
    }

    public static ArenaTeam fromTeam(PlayerTeam playerTeam) {
        if (playerTeam == null) return null;
        for (ArenaTeam team : values()) {
            if (playerTeam.getName().equals(team.getSerializedName())) {
                return team;
            }
        }
        return null;
    }

    public void setThisTeam(ServerPlayer player) {
        Scoreboard scoreboard = player.serverLevel().getScoreboard();
        PlayerTeam team = scoreboard.getPlayerTeam(getSerializedName());
        if (team == null) {
            team = scoreboard.addPlayerTeam(getSerializedName());
        }
        team.setColor(chatFormatting);
        team.setCollisionRule(Team.CollisionRule.PUSH_OTHER_TEAMS);
        team.setDisplayName(getColoredName());
        team.setSeeFriendlyInvisibles(true);
        team.setNameTagVisibility(ServerConfig.HIDE_ENEMY_NAMETAGS.get() ? Team.Visibility.HIDE_FOR_OTHER_TEAMS : Team.Visibility.ALWAYS);
        team.setAllowFriendlyFire(ServerConfig.FRIENDLY_FIRE.get());
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);
        Notification.toPlayer(Component.translatable("arena.message.switch_team", getName()).withColor(color), player);
    }

    public Component getScoreText(int value) {
        return Component.literal(getName() + ": " + value).withColor(color);
    }

    public int getColor() {
        return color;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

    public String getName() {
        return ArenaUtils.toTitleCase(name());
    }

    public Component getColoredName() {
        return Component.literal(getName()).withColor(color);
    }

    public DyeColor getDye() {
        return dyeColor;
    }
}
