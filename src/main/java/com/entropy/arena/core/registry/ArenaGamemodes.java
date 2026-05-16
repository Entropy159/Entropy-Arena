package com.entropy.arena.core.registry;

import com.entropy.arena.api.registrate.GamemodeEntry;
import com.entropy.arena.core.gamemodes.*;

import static com.entropy.arena.core.EntropyArena.REGISTRATE;

public class ArenaGamemodes {
    public static final GamemodeEntry<CaptureTheFlag> CAPTURE_THE_FLAG = REGISTRATE.gamemode("capture_the_flag", CaptureTheFlag::new).register();
    public static final GamemodeEntry<Disguise> DISGUISE = REGISTRATE.gamemode("disguise", Disguise::new).register();
    public static final GamemodeEntry<Domination> DOMINATION = REGISTRATE.gamemode("disguise", Domination::new).register();
    public static final GamemodeEntry<FreeForAll> FREE_FOR_ALL = REGISTRATE.gamemode("disguise", FreeForAll::new).register();
    public static final GamemodeEntry<KingOfTheHill> KING_OF_THE_HILL = REGISTRATE.gamemode("disguise", KingOfTheHill::new).register();
    public static final GamemodeEntry<Progression> PROGRESSION = REGISTRATE.gamemode("disguise", Progression::new).register();
    public static final GamemodeEntry<TeamDeathmatch> TEAM_DEATHMATCH = REGISTRATE.gamemode("disguise", TeamDeathmatch::new).register();
    public static final GamemodeEntry<WaveSurvival> WAVE_SURVIVAL = REGISTRATE.gamemode("disguise", WaveSurvival::new).register();

    public static void init() {
    }
}
