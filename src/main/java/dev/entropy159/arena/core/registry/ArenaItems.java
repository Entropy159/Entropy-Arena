package dev.entropy159.arena.core.registry;

import dev.entropy159.arena.api.util.ArenaTeam;
import dev.entropy159.arena.core.items.DisguiseItem;
import dev.entropy159.arena.core.items.TeamGemItem;
import com.tterrag.registrate.util.entry.ItemEntry;

import static dev.entropy159.arena.core.EntropyArena.REGISTRATE;

public class ArenaItems {
    public static final ItemEntry<TeamGemItem> TEAM_GEM = REGISTRATE.item("team_gem", props -> new TeamGemItem()).color(() -> () -> (stack, index) -> stack.getOrDefault(ArenaDataComponents.TEAM, ArenaTeam.NONE).getColor()).register();
    public static final ItemEntry<DisguiseItem> DISGUISE_ITEM = REGISTRATE.item("disguise_item", props -> new DisguiseItem()).model((ctx, provider) -> provider.handheldItem(ctx.get())).register();

    public static void init() {
    }
}
