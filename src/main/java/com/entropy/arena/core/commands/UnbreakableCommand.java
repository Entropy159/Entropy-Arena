package com.entropy.arena.core.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;

import static net.minecraft.commands.Commands.literal;

public class UnbreakableCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("unbreakable").requires(ctx -> ctx.isPlayer() && ctx.hasPermission(2)).executes(ctx -> {
            if (ctx.getSource().getPlayer() != null) {
                ItemStack stack = ctx.getSource().getPlayer().getMainHandItem();
                if (stack.has(DataComponents.UNBREAKABLE)) {
                    stack.remove(DataComponents.UNBREAKABLE);
                    ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.removed_unbreakable"), false);
                } else {
                    stack.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
                    ctx.getSource().sendSuccess(() -> Component.translatable("arena.message.added_unbreakable"), false);
                }
            }
            return 1;
        }));
    }
}
