package com.entropy.arena.core.items;

import com.entropy.arena.api.data.ArenaData;
import com.entropy.arena.core.gamemodes.CaptureTheFlag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class TeamGemItem extends Item {
    public TeamGemItem() {
        super(new Properties().stacksTo(1));
    }

    public void reset(ServerLevel level, ItemStack stack) {
        if (ArenaData.get(level).currentGamemode instanceof CaptureTheFlag ctf) {
            ctf.resetGem(level, stack);
        }
    }
}
