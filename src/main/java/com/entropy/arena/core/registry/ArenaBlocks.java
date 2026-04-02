package com.entropy.arena.core.registry;

import com.entropy.arena.api.ArenaTeam;
import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.blocks.*;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;

import java.util.HashMap;

import static com.entropy.arena.core.EntropyArena.REGISTRATE;

public class ArenaBlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreativeModeTabs.OP_BLOCKS);
    }

    public static final BlockEntry<KillBarrierBlock> KILL_BARRIER = REGISTRATE.block("kill_barrier", props -> new KillBarrierBlock()).blockstate((ctx, provider) -> provider.getVariantBuilder(ctx.get()).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(provider.models().withExistingParent(ctx.getName(), "block/barrier").texture("particle", EntropyArena.id("item/kill_barrier")))})).item().model((ctx, provider) -> provider.generated(ctx::get)).build().register();
    public static final BlockEntry<SpawnpointBlock> SPAWNPOINT = REGISTRATE.block("spawnpoint", props -> new SpawnpointBlock()).blockstate((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models().withExistingParent(ctx.getName(), EntropyArena.id("block/base_tint_translucent")).texture("all", "entropyarena:block/spawnpoint"))).color(() -> () -> (state, level, pos, index) -> state.hasProperty(SpawnpointBlock.SPAWN_COLOR) ? state.getValue(SpawnpointBlock.SPAWN_COLOR).getColor() : 0xFFFFFFFF).simpleItem().register();
    public static final BlockEntry<CapturePointBlock> CAPTURE_POINT = REGISTRATE.block("capture_point", props -> new CapturePointBlock()).blockstate((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models().cubeAll(ctx.getName(), provider.blockTexture(ctx.get())).renderType("translucent"))).simpleItem().register();
    public static final BlockEntry<PedestalBlock> PEDESTAL = REGISTRATE.block("pedestal", props -> new PedestalBlock()).blockstate((ctx, provider) -> provider.getVariantBuilder(ctx.get()).partialState().with(PedestalBlock.HAS_GEM, true).modelForState().modelFile(provider.models().withExistingParent(ctx.getName(), EntropyArena.id("block/base_" + ctx.getName() + "_on"))).addModel().partialState().with(PedestalBlock.HAS_GEM, false).modelForState().modelFile(provider.models().withExistingParent(ctx.getName() + "_off", EntropyArena.id("block/base_" + ctx.getName() + "_off"))).addModel()).color(() -> () -> (state, level, pos, index) -> state.hasProperty(PedestalBlock.GEM_COLOR) ? state.getValue(PedestalBlock.GEM_COLOR).getColor() : 0xFFFFFFFF).simpleItem().register();
    public static final HashMap<ArenaTeam, BlockEntry<TeamBlock>> TEAM_BLOCKS = new HashMap<>();

    public static void init() {
        for (ArenaTeam team : ArenaTeam.values()) {
            TEAM_BLOCKS.put(team, REGISTRATE.block(team.getSerializedName() + "_team_block", props -> new TeamBlock()).blockstate((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models().withExistingParent(ctx.getName(), EntropyArena.id("block/base_tint")).texture("all", "entropyarena:block/team_block"))).color(() -> () -> (state, level, pos, index) -> team.getColor()).item().color(() -> () -> (stack, index) -> team.getColor()).build().register());
        }
    }
}
