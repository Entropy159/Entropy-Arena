package com.entropy.arena.core;

import com.entropy.arena.api.ArenaUtils;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.registry.ArenaBlocks;
import com.entropy.arena.core.registry.ArenaSounds;
import com.entropy.arena.core.registry.ArenaTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static com.entropy.arena.core.EntropyArena.MODID;
import static com.entropy.arena.core.EntropyArena.REGISTRATE;

@EventBusSubscriber
public class ArenaDatagen {
    @SubscribeEvent
    public static void datagen(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new SoundDatagen(output, existingFileHelper));
        generator.addProvider(event.includeServer(), new TagDatagen(output, lookupProvider, existingFileHelper));
    }

    public static class TagDatagen extends BlockTagsProvider {
        public TagDatagen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
            super(output, lookupProvider, MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.@NotNull Provider provider) {
            tag(ArenaTags.TEAM_BLOCK_INVALID).add(Blocks.BARRIER, ArenaBlocks.KILL_BARRIER.get());
        }
    }

    public static class SoundDatagen extends SoundDefinitionsProvider {
        final Path musicFolder;

        protected SoundDatagen(PackOutput output, ExistingFileHelper helper) {
            super(output, MODID, helper);
            musicFolder = output.getOutputFolder().getParent().getParent().resolve("main/resources/assets/" + MODID + "/sounds/music");
        }

        @Override
        public void registerSounds() {
            SoundDefinition arena = SoundDefinition.definition();
            File arenaFolder = new File(musicFolder.resolve("arena").toUri());
            if (arenaFolder.exists()) {
                File[] files = arenaFolder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ogg"));
                if (files != null) {
                    for (File music : files) {
                        arena.with(sound(EntropyArena.id("music/arena/" + music.getName().replaceAll(".ogg", "")), SoundDefinition.SoundType.SOUND).stream());
                    }
                }
            }
            add(ArenaSounds.ARENA_SOUND, arena);

            SoundDefinition lobby = SoundDefinition.definition();
            File lobbyFolder = new File(musicFolder.resolve("lobby").toUri());
            if (lobbyFolder.exists()) {
                File[] files = lobbyFolder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ogg"));
                if (files != null) {
                    for (File music : files) {
                        lobby.with(sound(EntropyArena.id("music/lobby/" + music.getName().replaceAll(".ogg", "")), SoundDefinition.SoundType.SOUND).stream());
                    }
                }
            }
            add(ArenaSounds.LOBBY_SOUND, lobby);
        }
    }

    public static void addLang() {
        Path musicFolder = Path.of("").toAbsolutePath().getParent().getParent().resolve("src/main/resources/assets/" + MODID + "/sounds/music");
        File arenaFolder = new File(musicFolder.resolve("arena").toUri());
        if (arenaFolder.exists()) {
            File[] files = arenaFolder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ogg"));
            if (files != null) {
                for (File music : files) {
                    String name = music.getName().replaceAll(".ogg", "");
                    REGISTRATE.addRawLang(MODID + ":music/arena/" + name, ArenaUtils.toTitleCase(name));
                }
            }
        }

        File lobbyFolder = new File(musicFolder.resolve("lobby").toUri());
        if (lobbyFolder.exists()) {
            File[] files = lobbyFolder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ogg"));
            if (files != null) {
                for (File music : files) {
                    String name = music.getName().replaceAll(".ogg", "");
                    REGISTRATE.addRawLang(MODID + ":music/lobby/" + name, ArenaUtils.toTitleCase(name));
                }
            }
        }

        GamemodeRegistry.forEach(ArenaGamemode::generateLang);

        REGISTRATE.addRawLang("key.categories." + MODID, "Entropy Arena");
        REGISTRATE.addRawLang("key.next_music", "Next Music");
        REGISTRATE.addRawLang("key.map_voting", "Map Voting");
        REGISTRATE.addRawLang("key.loadouts", "Loadouts");

        REGISTRATE.addRawLang(MODID + ".configuration.infiniteBlocks", "Infinite Blocks");

        REGISTRATE.addRawLang(MODID + ".configuration.useChatForNotifications", "Use Chat For Notifications");
        REGISTRATE.addRawLang(MODID + ".configuration.notificationFadeoutDelay", "Notification Fade Out Delay");
        REGISTRATE.addRawLang(MODID + ".configuration.notificationFadeoutDuration", "Notification Fade Out Duration");

        REGISTRATE.addRawLang(MODID + ".configuration.concurrentChunkLoads", "Concurrent Chunk Loads");
        REGISTRATE.addRawLang(MODID + ".configuration.intervalSeconds", "Interval Seconds");
        REGISTRATE.addRawLang(MODID + ".configuration.recapSeconds", "Recap Seconds");
        REGISTRATE.addRawLang(MODID + ".configuration.defaultRoundSeconds", "Default Round Seconds");
        REGISTRATE.addRawLang(MODID + ".configuration.defaultTargetScore", "Default Target Score");
        REGISTRATE.addRawLang(MODID + ".configuration.friendlyFire", "Friendly Fire");
        REGISTRATE.addRawLang(MODID + ".configuration.hideEnemyNametags", "Hide Enemy Nametags");
        REGISTRATE.addRawLang(MODID + ".configuration.respawnDelay", "Respawn Delay");
        REGISTRATE.addRawLang(MODID + ".configuration.giveSaturation", "Give Saturation");
        REGISTRATE.addRawLang(MODID + ".configuration.returnAllGems", "Return All Gems");
        REGISTRATE.addRawLang(MODID + ".configuration.flagExpirationSeconds", "Flag Expiration Seconds");
        REGISTRATE.addRawLang(MODID + ".configuration.spawnProtection", "Spawn Protection");

        REGISTRATE.addRawLang("arena.hud.interval", "Interval: %s");
        REGISTRATE.addRawLang("arena.hud.timer", "Timer: %s");
        REGISTRATE.addRawLang("arena.hud.target_score", "Target score: %s");
        REGISTRATE.addRawLang("arena.hud.score_value", "Score: %s");

        REGISTRATE.addRawLang("arena.screen.voting", "Voting");
        REGISTRATE.addRawLang("arena.screen.loadout", "Loadouts");

        REGISTRATE.addRawLang("arena.tooltip.item_list", "From Item List: %s");

        REGISTRATE.addRawLang("arena.message.match_start", "Started match");
        REGISTRATE.addRawLang("arena.message.match_stop", "Stopped match");
        REGISTRATE.addRawLang("arena.message.set_lobby_pos", "Set lobby position to %s");
        REGISTRATE.addRawLang("arena.message.added_map", "Added map %s");
        REGISTRATE.addRawLang("arena.message.removed_map", "Removed map %s");
        REGISTRATE.addRawLang("arena.message.updated_map", "Updated map %s");
        REGISTRATE.addRawLang("arena.message.updated_map_overrides", "Updated map overrides for %s");
        REGISTRATE.addRawLang("arena.message.loaded_map", "Loaded map %s");
        REGISTRATE.addRawLang("arena.message.game_over", "Game over");
        REGISTRATE.addRawLang("arena.message.game_start", "Game starting");
        REGISTRATE.addRawLang("arena.message.voted_for_map", "Voted for map %s");
        REGISTRATE.addRawLang("arena.message.voted_for_timed", "Voted for timed match");
        REGISTRATE.addRawLang("arena.message.voted_for_score", "Voted for score match");
        REGISTRATE.addRawLang("arena.message.enable", "Starting Entropy Arena");
        REGISTRATE.addRawLang("arena.message.map_info", "Map: %s - gamemode: ");
        REGISTRATE.addRawLang("arena.message.switch_team", "You are on team %s");
        REGISTRATE.addRawLang("arena.message.respawning", "Respawning in %s");
        REGISTRATE.addRawLang("arena.message.nobody_scored", "Nobody scored anything, so nobody wins");
        REGISTRATE.addRawLang("arena.message.game_tied", "Game ended in a tie");
        REGISTRATE.addRawLang("arena.message.player_winner", "%s won the game with %s points");
        REGISTRATE.addRawLang("arena.message.team_winner", "Team %s has won the game with %s points");
        REGISTRATE.addRawLang("arena.message.collective_winner", "Final score was %s");
        REGISTRATE.addRawLang("arena.message.capture_point_progress", "Capturing point - %s%%");
        REGISTRATE.addRawLang("arena.message.capture_point_holding", "Holding capture point");
        REGISTRATE.addRawLang("arena.message.capture_point_contested", "Capture point is contested");
        REGISTRATE.addRawLang("arena.message.capture_point_taken", "%s has taken a capture point");
        REGISTRATE.addRawLang("arena.message.team_capture_point_taken", "Team %s has taken a capture point");
        REGISTRATE.addRawLang("arena.message.added_loadout", "Added loadout %s");
        REGISTRATE.addRawLang("arena.message.removed_loadout", "Removed loadout %s");
        REGISTRATE.addRawLang("arena.message.updated_loadout", "Updated loadout %s");
        REGISTRATE.addRawLang("arena.message.selected_loadout", "Selected loadout %s");
        REGISTRATE.addRawLang("arena.message.gave_loadout", "Gave loadout %s");
        REGISTRATE.addRawLang("arena.message.added_item_list", "Added item list %s");
        REGISTRATE.addRawLang("arena.message.removed_item_list", "Removed item list %s");
        REGISTRATE.addRawLang("arena.message.saved_item_list", "Saved item list %s");
        REGISTRATE.addRawLang("arena.message.loaded_item_list", "Loaded item list %s");
        REGISTRATE.addRawLang("arena.message.gave_item_list", "Gave item list %s");
        REGISTRATE.addRawLang("arena.message.chunk_load_progress", "Loaded chunk %s/%s");
        REGISTRATE.addRawLang("arena.message.chunk_reset_progress", "Reset chunk %s/%s");
        REGISTRATE.addRawLang("arena.message.added_unbreakable", "Make item unbreakable");
        REGISTRATE.addRawLang("arena.message.removed_unbreakable", "Make item breakable");

        REGISTRATE.addRawLang("arena.error.already_running", "Game is already running");
        REGISTRATE.addRawLang("arena.error.no_lobby", "No lobby position found");
        REGISTRATE.addRawLang("arena.error.no_maps", "No maps found");
        REGISTRATE.addRawLang("arena.error.no_gamemode", "Gamemode %s not found");
        REGISTRATE.addRawLang("arena.error.map_already_exists", "Map %s already exists");
        REGISTRATE.addRawLang("arena.error.map_not_found", "No map found with name %s");
        REGISTRATE.addRawLang("arena.error.no_spawns", "No valid spawns found");
        REGISTRATE.addRawLang("arena.error.team_not_found", "Team %s not found");
        REGISTRATE.addRawLang("arena.error.not_enough_teams", "Not enough team spawns found");
        REGISTRATE.addRawLang("arena.error.no_enemy_spawns", "No enemy spawns found");
        REGISTRATE.addRawLang("arena.error.no_capture_points", "No capture points found");
        REGISTRATE.addRawLang("arena.error.too_many_capture_points", "Too many capturep points found, limit is %s");
        REGISTRATE.addRawLang("arena.error.no_loadouts", "No loadouts found");
        REGISTRATE.addRawLang("arena.error.loadout_already_exists", "Loadout %s already exists");
        REGISTRATE.addRawLang("arena.error.loadout_not_found", "Loadout %s not found");;
        REGISTRATE.addRawLang("arena.error.item_list_already_exists", "Item list %s already exists");
        REGISTRATE.addRawLang("arena.error.item_list_not_found", "Item list %s not found");
        REGISTRATE.addRawLang("arena.error.no_inventory_at_pos", "No block with inventory at %s");
    }
}
