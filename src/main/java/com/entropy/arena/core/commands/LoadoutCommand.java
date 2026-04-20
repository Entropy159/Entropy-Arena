package com.entropy.arena.core.commands;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.loadout.Loadout;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class LoadoutCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("loadout")
                .requires(ctx -> ctx.isPlayer() && ctx.hasPermission(3))
                .then(literal("add")
                        .then(argument("name", StringArgumentType.string())
                                .executes(LoadoutCommand::addLoadout)))
                .then(literal("give")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(LOADOUT_SUGGESTIONS)
                                .executes(LoadoutCommand::giveLoadout)))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(LOADOUT_SUGGESTIONS)
                                .executes(LoadoutCommand::removeLoadout)))
                .then(literal("update")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(LOADOUT_SUGGESTIONS)
                                .executes(LoadoutCommand::updateLoadout)))
                .then(literal("enable")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(ENABLED_LOADOUT_SUGGESTIONS)
                                .executes(ctx -> setEnabled(ctx, true))))
                .then(literal("disable")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(DISABLED_LOADOUT_SUGGESTIONS)
                                .executes(ctx -> setEnabled(ctx, false))))
                .then(literal("list")
                        .executes(LoadoutCommand::listLoadouts)));
    }

    private static int addLoadout(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        String name = StringArgumentType.getString(ctx, "name");
        if (data.loadouts.containsKey(name)) {
            ctx.getSource().sendFailure(Component.translatable("arena.error.loadout_already_exists", name));
            return 0;
        }
        if (ctx.getSource().getPlayer() != null) {
            data.loadouts.put(name, new Loadout(ctx.getSource().getPlayer()));
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.added_loadout", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("permissions.requires.player"));
        return 0;
    }

    private static int removeLoadout(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        String name = StringArgumentType.getString(ctx, "name");
        if (data.loadouts.containsKey(name)) {
            data.loadouts.remove(name);
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.removed_loadout", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.loadout_not_found", name));
        return 0;
    }

    private static int giveLoadout(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        String name = StringArgumentType.getString(ctx, "name");
        if (data.loadouts.containsKey(name) && ctx.getSource().getPlayer() != null) {
            data.loadouts.get(name).giveToPlayer(ctx.getSource().getPlayer());
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.gave_loadout", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.loadout_not_found", name));
        return 0;
    }

    private static int updateLoadout(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        String name = StringArgumentType.getString(ctx, "name");
        if (data.loadouts.containsKey(name) && ctx.getSource().getPlayer() != null) {
            data.loadouts.put(name, new Loadout(ctx.getSource().getPlayer()));
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.updated_loadout", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.loadout_not_found", name));
        return 0;
    }

    private static int setEnabled(CommandContext<CommandSourceStack> ctx, boolean enable) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        String name = StringArgumentType.getString(ctx, "name");
        if (data.loadouts.containsKey(name)) {
            data.loadouts.get(name).setEnabled(enable);
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.%s_loadout".formatted(enable ? "enabled" : "disabled"), name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.loadout_not_found", name));
        return 0;
    }

    private static int listLoadouts(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.loadouts.isEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("arena.error.no_loadouts"));
            return 0;
        }
        data.loadouts.keySet().forEach(name -> ctx.getSource().sendSuccess(() -> Component.literal(name).withStyle(ChatFormatting.GREEN), false));
        return 1;
    }

    private static final SuggestionProvider<CommandSourceStack> LOADOUT_SUGGESTIONS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.loadouts.keySet().forEach(name -> builder.suggest("\"" + name + "\""));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> ENABLED_LOADOUT_SUGGESTIONS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.loadouts.forEach((key, value) -> {
            if (value.isEnabled()) {
                builder.suggest("\"" + key + "\"");
            }
        });
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> DISABLED_LOADOUT_SUGGESTIONS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.loadouts.forEach((key, value) -> {
            if (!value.isEnabled()) {
                builder.suggest("\"" + key + "\"");
            }
        });
        return builder.buildFuture();
    };
}
