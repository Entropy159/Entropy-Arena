package dev.entropy159.arena.core;

import dev.entropy159.arena.core.config.ClientConfig;
import dev.entropy159.arena.core.config.ServerConfig;
import dev.entropy159.arena.core.registry.ArenaBlocks;
import dev.entropy159.arena.core.registry.ArenaSounds;
import dev.entropy159.arena.core.registry.ArenaTags;
import dev.entropy159.entropylib.util.Utils;
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

import static dev.entropy159.arena.core.EntropyArena.MODID;
import static dev.entropy159.arena.core.EntropyArena.REGISTRATE;

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
                        arena.with(sound(EntropyArena.id("music/arena/" + music.getName().replaceAll(".ogg", ""))).stream().volume(0.5));
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
                        lobby.with(sound(EntropyArena.id("music/lobby/" + music.getName().replaceAll(".ogg", ""))).stream().volume(0.5));
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
                    REGISTRATE.addRawLang(MODID + ":music/arena/" + name, Utils.toTitleCase(name));
                }
            }
        }

        File lobbyFolder = new File(musicFolder.resolve("lobby").toUri());
        if (lobbyFolder.exists()) {
            File[] files = lobbyFolder.listFiles(f -> !f.isDirectory() && f.getName().endsWith(".ogg"));
            if (files != null) {
                for (File music : files) {
                    String name = music.getName().replaceAll(".ogg", "");
                    REGISTRATE.addRawLang(MODID + ":music/lobby/" + name, Utils.toTitleCase(name));
                }
            }
        }

        REGISTRATE.addRawLang("key.categories." + MODID, "Entropy Arena");
        REGISTRATE.addRawLang("key.next_music", "Next Music");
        REGISTRATE.addRawLang("key.map_voting", "Map Voting");
        REGISTRATE.addRawLang("key.loadouts", "Loadouts");

        REGISTRATE.configLang("title", "Entropy Arena");

        REGISTRATE.configLang("section." + MODID + ".client.toml", "Client");
        REGISTRATE.configLang("section." + MODID + ".client.toml.title", "Client");
        REGISTRATE.configLang(ClientConfig.USE_CHAT_FOR_NOTIFICATIONS, "Use Chat For Notifications");
        REGISTRATE.configLang(ClientConfig.NOTIFICATION_FADEOUT_DELAY, "Notification Fade Out Delay");
        REGISTRATE.configLang(ClientConfig.NOTIFICATION_FADEOUT_DURATION, "Notification Fade Out Duration");

        REGISTRATE.configLang("section." + MODID + ".server.toml", "Server");
        REGISTRATE.configLang("section." + MODID + ".server.toml.title", "Server");
        REGISTRATE.configLang(ServerConfig.CONCURRENT_CHUNK_LOADS, "Concurrent Chunk Loads");
        REGISTRATE.configLang(ServerConfig.INTERVAL_SECONDS, "Interval Seconds");
        REGISTRATE.configLang(ServerConfig.RECAP_SECONDS, "Recap Seconds");
        REGISTRATE.configLang(ServerConfig.ROUND_SECONDS, "Round Seconds");
        REGISTRATE.configLang(ServerConfig.TARGET_SCORE, "Target Score");
        REGISTRATE.configLang(ServerConfig.FRIENDLY_FIRE, "Friendly Fire");
        REGISTRATE.configLang(ServerConfig.HIDE_ENEMY_NAMETAGS, "Hide Enemy Nametags");
        REGISTRATE.configLang(ServerConfig.RESPAWN_DELAY, "Respawn Delay");
        REGISTRATE.configLang(ServerConfig.GIVE_SATURATION, "Give Saturation");
        REGISTRATE.configLang(ServerConfig.INFINITE_BLOCKS, "Infinite Blocks");
        REGISTRATE.configLang(ServerConfig.ALLOW_BLOCKS, "Allow Blocks");
        REGISTRATE.configLang(ServerConfig.SPAWN_PROTECTION, "Spawn Protection");
        REGISTRATE.configLang(ServerConfig.MAX_HEALTH, "Max Health");
        REGISTRATE.configLang(ServerConfig.PREVENT_BLOCKS_ON_SPAWNS, "Prevent Blocks on Spawns");
        REGISTRATE.configLang(ServerConfig.SET_WORLD_BORDER, "Set World Border");

        REGISTRATE.configLang(ServerConfig.DEDUCT_POINTS_ON_SELF_DEATH, "Deduct Points on Self Death");

        REGISTRATE.configLang(ServerConfig.KILL_STREAK_LOSE_ANNOUNCE, "Kill Streak Lose Announce");
        REGISTRATE.configLang(ServerConfig.KILL_STREAK_ANNOUNCEMENTS, "Kill Streak Announcements");
        REGISTRATE.configLang("killStreakAnnouncements.button", "-->");

        REGISTRATE.configLang("ctf", "Capture the Flag");
        REGISTRATE.configLang("ctf.button", "-->");
        REGISTRATE.configLang(ServerConfig.REQUIRE_GEM_TO_SCORE, "Require Gem to Score");
        REGISTRATE.configLang(ServerConfig.TEAM_SWITCH_COOLDOWN, "Team Switch Cooldown");
        REGISTRATE.configLang(ServerConfig.GLOWING_FOR_FLAG, "Glowing for Flag");
        REGISTRATE.configLang(ServerConfig.RETURN_ALL_GEMS, "Return All Gems");
        REGISTRATE.configLang(ServerConfig.FLAG_EXPIRATION_SECONDS, "Flag Expiration Seconds");

        REGISTRATE.addRawLang("hud.arena.interval", "Interval: %s");
        REGISTRATE.addRawLang("hud.arena.timer", "Timer: %s");
        REGISTRATE.addRawLang("hud.arena.target_score", "Target score: %s");
        REGISTRATE.addRawLang("hud.arena.score_value", "Score: %s");
        REGISTRATE.addRawLang("hud.arena.votes", "Votes: %s");

        REGISTRATE.addRawLang("arena.type.timed", "Timed");
        REGISTRATE.addRawLang("arena.type.score", "Score");

        REGISTRATE.addRawLang("screen.arena.voting", "Voting");
        REGISTRATE.addRawLang("screen.arena.loadout", "Loadouts");

        REGISTRATE.addRawLang("tooltip.arena.item_list", "From Item List: %s");

        REGISTRATE.addRawLang("arena.enabled", "Enabled");
        REGISTRATE.addRawLang("arena.disabled", "Disabled");

        REGISTRATE.addRawLang("message.arena.match_start", "Started match");
        REGISTRATE.addRawLang("message.arena.match_stop", "Stopped match");
        REGISTRATE.addRawLang("message.arena.set_lobby_pos", "Set lobby position to %s");
        REGISTRATE.addRawLang("message.arena.added_map", "Added map %s");
        REGISTRATE.addRawLang("message.arena.removed_map", "Removed map %s");
        REGISTRATE.addRawLang("message.arena.updated_map", "Updated map %s");
        REGISTRATE.addRawLang("message.arena.updated_map_config", "Updated map config for %s - %s set to %s");
        REGISTRATE.addRawLang("message.arena.reset_map_config", "Reset map config for %s - %s");
        REGISTRATE.addRawLang("message.arena.loaded_map", "Loaded map %s");
        REGISTRATE.addRawLang("message.arena.enabled_map", "Enabled map %s");
        REGISTRATE.addRawLang("message.arena.disabled_map", "Disabled map %s");
        REGISTRATE.addRawLang("message.arena.game_over", "Game over");
        REGISTRATE.addRawLang("message.arena.game_start", "Game starting");
        REGISTRATE.addRawLang("message.arena.voted_for_map", "Voted for map %s");
        REGISTRATE.addRawLang("message.arena.voted_for_type", "Voted for %s match");
        REGISTRATE.addRawLang("message.arena.enable", "Starting Entropy Arena");
        REGISTRATE.addRawLang("message.arena.map_info", "Map: %s - gamemode: ");
        REGISTRATE.addRawLang("message.arena.switch_team", "You are on team %s");
        REGISTRATE.addRawLang("message.arena.respawning", "Respawning in %s");
        REGISTRATE.addRawLang("message.arena.nobody_scored", "Nobody scored anything, so nobody wins");
        REGISTRATE.addRawLang("message.arena.game_tied", "Game ended in a tie");
        REGISTRATE.addRawLang("message.arena.player_winner", "%s won the game with %s points");
        REGISTRATE.addRawLang("message.arena.team_winner", "Team %s has won the game with %s points");
        REGISTRATE.addRawLang("message.arena.collective_winner", "Final score was %s");
        REGISTRATE.addRawLang("message.arena.capture_point_progress", "Capturing point - %s%%");
        REGISTRATE.addRawLang("message.arena.capture_point_holding", "Holding capture point");
        REGISTRATE.addRawLang("message.arena.capture_point_contested", "Capture point is contested");
        REGISTRATE.addRawLang("message.arena.capture_point_taken", "%s has taken a capture point");
        REGISTRATE.addRawLang("message.arena.team_capture_point_taken", "Team %s has taken a capture point");
        REGISTRATE.addRawLang("message.arena.added_loadout", "Added loadout %s");
        REGISTRATE.addRawLang("message.arena.removed_loadout", "Removed loadout %s");
        REGISTRATE.addRawLang("message.arena.updated_loadout", "Updated loadout %s");
        REGISTRATE.addRawLang("message.arena.selected_loadout", "Selected loadout %s");
        REGISTRATE.addRawLang("message.arena.gave_loadout", "Gave loadout %s");
        REGISTRATE.addRawLang("message.arena.enabled_loadout", "Enabled loadout %s");
        REGISTRATE.addRawLang("message.arena.disabled_loadout", "Disabled loadout %s");
        REGISTRATE.addRawLang("message.arena.added_item_list", "Added item list %s");
        REGISTRATE.addRawLang("message.arena.removed_item_list", "Removed item list %s");
        REGISTRATE.addRawLang("message.arena.saved_item_list", "Saved item list %s");
        REGISTRATE.addRawLang("message.arena.loaded_item_list", "Loaded item list %s");
        REGISTRATE.addRawLang("message.arena.gave_item_list", "Gave item list %s");
        REGISTRATE.addRawLang("message.arena.chunk_load_progress", "Loaded chunk %s/%s");
        REGISTRATE.addRawLang("message.arena.chunk_reset_progress", "Reset chunk %s/%s");
        REGISTRATE.addRawLang("message.arena.added_unbreakable", "Made item unbreakable");
        REGISTRATE.addRawLang("message.arena.removed_unbreakable", "Made item breakable");
        REGISTRATE.addRawLang("message.arena.switched_team", "%s has switched to team %s");
        REGISTRATE.addRawLang("message.arena.lost_killstreak", "%s has lost their kill streak of %s");

        REGISTRATE.addRawLang("error.arena.already_running", "Game is already running");
        REGISTRATE.addRawLang("error.arena.no_lobby", "No lobby position found");
        REGISTRATE.addRawLang("error.arena.no_maps", "No maps found");
        REGISTRATE.addRawLang("error.arena.no_gamemode", "Gamemode %s not found");
        REGISTRATE.addRawLang("error.arena.map_already_exists", "Map %s already exists");
        REGISTRATE.addRawLang("error.arena.map_not_found", "No map found with name %s");
        REGISTRATE.addRawLang("error.arena.no_spawns", "No valid spawns found");
        REGISTRATE.addRawLang("error.arena.team_not_found", "Team %s not found");
        REGISTRATE.addRawLang("error.arena.not_enough_teams", "Not enough team spawns found");
        REGISTRATE.addRawLang("error.arena.no_enemy_spawns", "No enemy spawns found");
        REGISTRATE.addRawLang("error.arena.no_capture_points", "No capture points found");
        REGISTRATE.addRawLang("error.arena.too_many_capture_points", "Too many capturep points found, limit is %s");
        REGISTRATE.addRawLang("error.arena.no_loadouts", "No loadouts found");
        REGISTRATE.addRawLang("error.arena.loadout_already_exists", "Loadout %s already exists");
        REGISTRATE.addRawLang("error.arena.loadout_not_found", "Loadout %s not found");
        REGISTRATE.addRawLang("error.arena.item_list_already_exists", "Item list %s already exists");
        REGISTRATE.addRawLang("error.arena.item_list_not_found", "Item list %s not found");
        REGISTRATE.addRawLang("error.arena.no_inventory_at_pos", "No block with inventory at %s");
        REGISTRATE.addRawLang("error.arena.no_ordered_item_lists", "No ordered item lists found");
        REGISTRATE.addRawLang("error.arena.no_random_item_lists", "No ordered item lists found");
        REGISTRATE.addRawLang("error.arena.backing_up", "Backing up map");
        REGISTRATE.addRawLang("error.arena.restoring_backup", "Restoring map backup");
        REGISTRATE.addRawLang("error.arena.cant_switch_teams", "Can't switch teams right now!");
        REGISTRATE.addRawLang("error.arena.no_config", "No config found with key %s and mod ID %s");
        REGISTRATE.addRawLang("error.arena.invalid_config_value", "Invalid config value %s");
        REGISTRATE.addRawLang("error.arena.no_level", "No level found with dimension %s");

        REGISTRATE.addRawLang("message.arena.ctf.flag_taken", "Team %s's flag has been taken by team %s");
        REGISTRATE.addRawLang("message.arena.ctf.flag_returned", "Team %s's flag has been returned");
        REGISTRATE.addRawLang("message.arena.ctf.flag_scored", "Team %s has scored");
        REGISTRATE.addRawLang("message.arena.ctf.flag_dropped", "Team %s's flag has dropped out of the map");
        REGISTRATE.addRawLang("message.arena.ctf.pedestal_invalid", "You cannot score on a pedestal that's been taken from");
        REGISTRATE.addRawLang("error.arena.ctf.not_enough_pedestals", "Not enough pedestals");
        REGISTRATE.addRawLang("error.arena.ctf.only_one_flag", "You can only have one flag at a time");

        REGISTRATE.addRawLang("message.arena.koth.new_king", "%s has taken the hill");
        REGISTRATE.addRawLang("message.arena.koth.hill_lost", "%s has lost the hill");

        REGISTRATE.addRawLang("message.arena.waves.interval", "Interval: %s");
        REGISTRATE.addRawLang("message.arena.waves.mob_count", "Mobs: %s/%s");
        REGISTRATE.addRawLang("message.arena.waves.survived_wave", "Survived wave %s");
    }
}
