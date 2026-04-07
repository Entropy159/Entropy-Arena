package com.entropy.arena.core.commands;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.api.map.ArenaMap;
import com.entropy.arena.api.map.MapList;
import com.entropy.arena.core.ArenaLogic;
import com.entropy.arena.core.network.toClient.TakeScreenshotPacket;
import com.entropy.arena.core.registry.ArenaDataComponents;
import com.entropy.arena.core.registry.ArenaItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ArenaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("arena")
                .requires(ctx -> ctx.hasPermission(3))
                .then(literal("start")
                        .executes(ArenaCommand::start))
                .then(literal("stop")
                        .executes(ArenaCommand::stop))
                .then(literal("setLobbyPos")
                        .then(argument("position", BlockPosArgument.blockPos())
                                .executes(ArenaCommand::setLobbyPos)))
                .then(literal("flag")
                        .requires(CommandSourceStack::isPlayer)
                        .then(argument("team", StringArgumentType.word())
                                .suggests(TEAM_SUGGESTIONS)
                                .then(argument("index", IntegerArgumentType.integer(0))
                                        .executes(ArenaCommand::getFlag))))
                .then(literal("map")
                        .then(literal("create")
                                .then(argument("name", StringArgumentType.string())
                                        .then(argument("gamemode", ResourceLocationArgument.id())
                                                .suggests(GAMEMODE_SUGGESTIONS)
                                                .then(argument("corner1", BlockPosArgument.blockPos())
                                                        .then(argument("corner2", BlockPosArgument.blockPos())
                                                                .executes(ArenaCommand::createMap))))))
                        .then(literal("remove")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(MAP_SUGGESTIONS)
                                        .executes(ArenaCommand::removeMap)))
                        .then(literal("update")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(MAP_SUGGESTIONS)
                                        .executes(ArenaCommand::updateMap)))
                        .then(literal("overrides")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(MAP_SUGGESTIONS)
                                        .then(argument("timer", IntegerArgumentType.integer(0, 1800))
                                                .then(argument("score", IntegerArgumentType.integer(0, 1000))
                                                        .executes(ArenaCommand::updateMapOverrides)))))
                        .then(literal("load")
                                .then(argument("name", StringArgumentType.string())
                                        .suggests(MAP_SUGGESTIONS)
                                        .executes(ArenaCommand::loadMap)))
                        .then(literal("list")
                                .executes(ArenaCommand::listMaps))));
    }

    private static int start(CommandContext<CommandSourceStack> ctx) {
        Component error = ArenaLogic.get(ctx.getSource().getLevel()).enable();
        if (error == null) {
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.match_start").withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            ctx.getSource().sendFailure(error);
        }
        return 0;
    }

    private static int stop(CommandContext<CommandSourceStack> ctx) {
        ArenaLogic.get(ctx.getSource().getLevel()).disable();
        ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.match_stop").withStyle(ChatFormatting.RED), true);
        return 1;
    }

    private static int setLobbyPos(CommandContext<CommandSourceStack> ctx) {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "position");
        data.lobbyPos = pos;
        ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.set_lobby_pos", pos.toShortString()).withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int createMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ResourceLocation gamemode = ResourceLocationArgument.getId(ctx, "gamemode");
        BlockPos pos1 = BlockPosArgument.getBlockPos(ctx, "corner1");
        BlockPos pos2 = BlockPosArgument.getBlockPos(ctx, "corner2");
        Component failureMessage = MapList.addMap(ctx.getSource().getLevel(), name, gamemode, pos1, pos2);
        if (failureMessage == null) {
            if (ctx.getSource().getPlayer() != null) {
                PacketDistributor.sendToPlayer(ctx.getSource().getPlayer(), new TakeScreenshotPacket(name));
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.added_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(failureMessage);
        return 0;
    }

    private static int removeMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        if (MapList.removeMap(name)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.removed_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int updateMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaMap map = MapList.getMap(name);
        if (map != null) {
            map.update(ctx.getSource().getLevel(), ctx.getSource().getPlayer());
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.updated_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int updateMapOverrides(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        int timer = IntegerArgumentType.getInteger(ctx, "timer");
        int score = IntegerArgumentType.getInteger(ctx, "score");
        ArenaMap map = MapList.getMap(name);
        if (map != null) {
            map.setOverrides(timer, score);
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.updated_map_overrides", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int loadMap(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaMap map = MapList.getMap(name);
        if (map != null) {
            map.load(ctx.getSource().getLevel());
            if (ctx.getSource().getPlayer() != null) {
                ctx.getSource().getPlayer().teleportTo(map.getCenter().x, map.getCenter().y, map.getCenter().z);
            }
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.loaded_map", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.map_not_found", name).withStyle(ChatFormatting.DARK_RED));
        return 0;
    }

    private static int listMaps(CommandContext<CommandSourceStack> ctx) {
        if (MapList.mapListIsEmpty()) {
            ctx.getSource().sendFailure(Component.translatable("arena.error.no_maps"));
            return 0;
        }
        MapList.forEachMap(map -> ctx.getSource().sendSuccess(map::toComponent, false));
        return 1;
    }

    private static int getFlag(CommandContext<CommandSourceStack> ctx) {
        String teamName = StringArgumentType.getString(ctx, "team");
        int flagIndex = IntegerArgumentType.getInteger(ctx, "index");
        if (ctx.getSource().getPlayer() != null) {
            try {
                ArenaTeam team = ArenaTeam.valueOf(teamName.toUpperCase());
                ItemStack flag = new ItemStack(ArenaItems.TEAM_GEM.get());
                flag.set(ArenaDataComponents.PEDESTAL_INDEX, flagIndex);
                flag.set(ArenaDataComponents.TEAM, team);
                ctx.getSource().getPlayer().addItem(flag);
                return 1;
            } catch (IllegalArgumentException e) {
                ctx.getSource().sendFailure(Component.translatable("arena.error.team_not_found", teamName));
                return 0;
            }
        }
        return 0;
    }

    private static final SuggestionProvider<CommandSourceStack> MAP_SUGGESTIONS = (ctx, builder) -> {
        MapList.forEachMap(map -> builder.suggest("\"" + map.getName() + "\""));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> GAMEMODE_SUGGESTIONS = (ctx, builder) -> {
        GamemodeRegistry.forEach(mode -> builder.suggest(mode.getRegistryID().toString()));
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> TEAM_SUGGESTIONS = (ctx, builder) -> {
        for (ArenaTeam team : ArenaTeam.values()) {
            builder.suggest(team.getSerializedName());
        }
        return builder.buildFuture();
    };
}
