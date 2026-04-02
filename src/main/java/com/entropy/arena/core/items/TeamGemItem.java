package com.entropy.arena.core.items;

import com.entropy.arena.api.data.ArenaLogic;
import com.entropy.arena.core.gamemodes.CaptureTheFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TeamGemItem extends Item {
    public TeamGemItem() {
        super(new Properties().stacksTo(1));
    }

    public void reset(ArenaLogic data, ItemStack stack) {
        if (data.getCurrentGamemode() instanceof CaptureTheFlag ctf) {
            ctf.resetGem(data.getLevel(), stack);
        }
    }
}
