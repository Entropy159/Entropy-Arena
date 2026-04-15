package com.entropy.arena.core.registry;

import com.entropy.arena.api.gamemode.GamemodeRegistry;
import com.entropy.arena.core.gamemodes.*;

public class ArenaGamemodes {
    public static void init() {
        GamemodeRegistry.registerGamemode(CaptureTheFlag::new);
        GamemodeRegistry.registerGamemode(Domination::new);
        GamemodeRegistry.registerGamemode(FreeForAll::new);
        GamemodeRegistry.registerGamemode(KingOfTheHill::new);
        GamemodeRegistry.registerGamemode(Progression::new);
        GamemodeRegistry.registerGamemode(TeamDeathmatch::new);
        GamemodeRegistry.registerGamemode(WaveSurvival::new);
    }
}
