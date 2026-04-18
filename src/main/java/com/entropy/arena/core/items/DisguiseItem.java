package com.entropy.arena.core.items;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.gamemodes.Disguise;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;

public class DisguiseItem extends Item {
    public DisguiseItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer player && ArenaData.get(player.serverLevel()).currentGamemode instanceof Disguise gamemode) {
            gamemode.setDisguise(player, player.serverLevel().getBlockState(context.getClickedPos()));
            return InteractionResult.SUCCESS_NO_ITEM_USED;
        }
        return super.useOn(context);
    }
}
