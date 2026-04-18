package com.entropy.arena.core.registry;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.items.DisguiseItem;
import com.entropy.arena.core.items.TeamGemItem;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.entropy.arena.core.EntropyArena.REGISTRATE;

public class ArenaItems {
    public static final ItemEntry<TeamGemItem> TEAM_GEM = REGISTRATE.item("team_gem", props -> new TeamGemItem()).color(() -> () -> (stack, index) -> stack.getOrDefault(ArenaDataComponents.TEAM, ArenaTeam.NONE).getColor()).register();
    public static final ItemEntry<DisguiseItem> DISGUISE_ITEM = REGISTRATE.item("disguise_item", props -> new DisguiseItem()).model((ctx, provider) -> provider.handheldItem(ctx.get())).register();

    public static void init() {
    }
}
