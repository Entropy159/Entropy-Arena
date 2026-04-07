package com.entropy.arena.core.commands;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.api.loadout.ItemList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class ItemListCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("itemlist")
                .then(literal("add")
                        .then(argument("name", StringArgumentType.string())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> addList(ctx, true)))))
                .then(literal("addOrdered")
                        .then(argument("name", StringArgumentType.string())
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> addList(ctx, false)))))
                .then(literal("remove")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(ITEM_LISTS)
                                .executes(ItemListCommand::removeList)))
                .then(literal("load")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(ITEM_LISTS)
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(ItemListCommand::loadList))))
                .then(literal("save")
                        .then(argument("name", StringArgumentType.string())
                                .suggests(ITEM_LISTS)
                                .then(argument("pos", BlockPosArgument.blockPos())
                                        .executes(ItemListCommand::saveList))))
                .then(literal("giveItem")
                        .requires(CommandSourceStack::isPlayer)
                        .then(argument("name", StringArgumentType.string())
                                .suggests(ITEM_LISTS)
                                .executes(ItemListCommand::getListItem))));
    }

    private static int addList(CommandContext<CommandSourceStack> ctx, boolean random) {
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (!data.itemLists.containsKey(name)) {
            data.itemLists.put(name, new ItemList(ctx.getSource().getLevel(), pos, random));
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.added_item_list", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.item_list_already_exists", name));
        return 0;
    }

    private static int removeList(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.itemLists.containsKey(name)) {
            data.itemLists.remove(name);
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.removed_item_list", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.item_list_not_found", name));
        return 0;
    }

    private static int loadList(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.itemLists.containsKey(name)) {
            IItemHandler handler = ctx.getSource().getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                data.itemLists.get(name).loadToBlock(handler);
                ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.loaded_item_list", name).withStyle(ChatFormatting.GREEN), true);
                return 1;
            }
            ctx.getSource().sendFailure(Component.translatable("arena.error.no_inventory_at_pos", pos));
            return 0;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.item_list_not_found", name));
        return 0;
    }

    private static int saveList(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        BlockPos pos = BlockPosArgument.getBlockPos(ctx, "pos");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.itemLists.containsKey(name)) {
            IItemHandler handler = ctx.getSource().getLevel().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler != null) {
                data.itemLists.get(name).saveFromBlock(handler);
                ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.saved_item_list", name).withStyle(ChatFormatting.GREEN), true);
                return 1;
            }
            ctx.getSource().sendFailure(Component.translatable("arena.error.no_inventory_at_pos", pos));
            return 0;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.item_list_not_found", name));
        return 0;
    }

    private static int getListItem(CommandContext<CommandSourceStack> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        if (data.itemLists.containsKey(name) && ctx.getSource().getPlayer() != null) {
            ctx.getSource().getPlayer().addItem(data.itemLists.get(name).getItem(name));
            ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.gave_item_list", name).withStyle(ChatFormatting.GREEN), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("arena.error.item_list_not_found", name));
        return 0;
    }

    private static final SuggestionProvider<CommandSourceStack> ITEM_LISTS = (ctx, builder) -> {
        ArenaData data = ArenaData.get(ctx.getSource().getLevel());
        data.itemLists.keySet().forEach(name -> builder.suggest("\"" + name + "\""));
        return builder.buildFuture();
    };
}
