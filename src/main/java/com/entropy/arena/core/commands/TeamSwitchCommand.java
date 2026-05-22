package com.entropy.arena.core.commands;

import com.entropy.arena.api.util.ArenaTeam;
import com.entropy.arena.api.util.Notification;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.ArenaLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TeamSwitchCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("switch").requires(CommandSourceStack::isPlayer).then(argument("team", StringArgumentType.word()).suggests(TEAM_SUGGESTIONS).executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayer();
            if (player != null) {
                try {
                    ArenaTeam team = ArenaTeam.valueOf(StringArgumentType.getString(ctx, "team").toUpperCase());
                    ArenaMap map = ArenaData.get(ctx.getSource().getLevel()).currentMap;
                    if (map != null) {
                        if (map.getTeams(ctx.getSource().getLevel()).contains(team)) {
                            if (ArenaData.get(ctx.getSource().getLevel()).currentGamemode instanceof TeamGamemode teamGamemode && teamGamemode.canSwitchToTeam(ctx.getSource().getPlayer(), team)) {
                                teamGamemode.setPlayerTeam(player, team);
                                teamGamemode.sendToAll();
                                ArenaLogic.get(ctx.getSource().getLevel()).respawn(player);
                                Notification.toAll(Component.translatable("arena.message.switched_team", player.getDisplayName(), team.getColoredName()).withStyle(ChatFormatting.YELLOW));
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(Component.translatable("arena.error.cant_switch_teams"));
                            }
                        } else {
                            ctx.getSource().sendFailure(Component.translatable("arena.error.team_not_found", team.getName()));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    ctx.getSource().sendFailure(Component.translatable("arena.error.team_not_found", StringArgumentType.getString(ctx, "team")));
                }
            }
            return 0;
        })));
    }

    private static final SuggestionProvider<CommandSourceStack> TEAM_SUGGESTIONS = (ctx, builder) -> {
        for (ArenaTeam team : ArenaTeam.values()) {
            ArenaMap map = ArenaData.get(ctx.getSource().getLevel()).currentMap;
            if (map != null && map.getTeams(ctx.getSource().getLevel()).contains(team)) {
                builder.suggest(team.getSerializedName());
            }
        }
        return builder.buildFuture();
    };
}
