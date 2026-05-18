package com.entropy.arena.core.registry;

import com.entropy.arena.api.registrate.GamemodeEntry;
import com.entropy.arena.core.gamemodes.*;

import static com.entropy.arena.core.EntropyArena.REGISTRATE;

public class ArenaGamemodes {
    public static final GamemodeEntry<CaptureTheFlag> CAPTURE_THE_FLAG = REGISTRATE.gamemode("capture_the_flag", CaptureTheFlag::new).register();
    public static final GamemodeEntry<Disguise> DISGUISE = REGISTRATE.gamemode("disguise", Disguise::new).register();
    public static final GamemodeEntry<Domination> DOMINATION = REGISTRATE.gamemode("domination", Domination::new).register();
    public static final GamemodeEntry<FreeForAll> FREE_FOR_ALL = REGISTRATE.gamemode("free_for_all", FreeForAll::new).register();
    public static final GamemodeEntry<KingOfTheHill> KING_OF_THE_HILL = REGISTRATE.gamemode("king_of_the_hill", KingOfTheHill::new).register();
    public static final GamemodeEntry<Progression> PROGRESSION = REGISTRATE.gamemode("progression", Progression::new).register();
    public static final GamemodeEntry<TeamDeathmatch> TEAM_DEATHMATCH = REGISTRATE.gamemode("team_deathmatch", TeamDeathmatch::new).register();
    public static final GamemodeEntry<WaveSurvival> WAVE_SURVIVAL = REGISTRATE.gamemode("wave_survival", WaveSurvival::new).register();

    public static void init() {
    }
}
