package com.entropy.arena.core.commands;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.TeamGamemode;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.core.ArenaLogic;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TeamSwitchCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("switch").requires(ctx -> ctx.isPlayer() && ArenaData.get(ctx.getLevel()).inGame() && ArenaData.get(ctx.getLevel()).currentGamemode instanceof TeamGamemode).then(argument("team", StringArgumentType.word()).suggests(TEAM_SUGGESTIONS).executes(ctx -> {
            if (ctx.getSource().getPlayer() != null) {
                try {
                    ArenaTeam team = ArenaTeam.valueOf(StringArgumentType.getString(ctx, "team").toUpperCase());
                    ArenaMap map = ArenaData.get(ctx.getSource().getLevel()).currentMap;
                    if (map != null) {
                        if (map.getTeams(ctx.getSource().getLevel()).contains(team)) {
                            if (ArenaData.get(ctx.getSource().getLevel()).currentGamemode instanceof TeamGamemode teamGamemode && teamGamemode.canSwitchToTeam(ctx.getSource().getPlayer(), team)) {
                                teamGamemode.setPlayerTeam(ctx.getSource().getPlayer(), team);
                                teamGamemode.sendToAll();
                                ArenaLogic.get(ctx.getSource().getLevel()).onRespawn(ctx.getSource().getPlayer());
                                return 1;
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
