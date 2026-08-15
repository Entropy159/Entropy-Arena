package dev.entropy159.arena.core.commands;

import dev.entropy159.arena.api.util.ArenaTeam;
import dev.entropy159.arena.api.util.Notification;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.gamemode.TeamGamemode;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.ArenaLogic;
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
                                Notification.toAll(Component.translatable("message.arena.switched_team", player.getDisplayName(), team.getColoredName()).withStyle(ChatFormatting.YELLOW));
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(Component.translatable("error.arena.cant_switch_teams"));
                            }
                        } else {
                            ctx.getSource().sendFailure(Component.translatable("error.arena.team_not_found", team.getName()));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    ctx.getSource().sendFailure(Component.translatable("error.arena.team_not_found", StringArgumentType.getString(ctx, "team")));
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
