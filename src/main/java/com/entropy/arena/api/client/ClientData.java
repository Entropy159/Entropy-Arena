package com.entropy.arena.api.client;

import com.entropy.arena.api.ArenaGameType;
import com.entropy.arena.api.Notification;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.map.ArenaMapInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientData {
    public static boolean running = false;
    public static boolean inLobby = true;
    public static int timer = 0;
    public static int targetScore = 0;
    public static String currentMap;
    public static long lastRespawn = 0;
    public static ArenaGamemode currentGamemode;

    public static List<ArenaMapInfo> votableMaps = new ArrayList<>();
    public static Map<ArenaGameType, Integer> typeVotes = new HashMap<>();
    public static ArrayList<String> loadouts = new ArrayList<>();
    public static ArrayList<Notification> notifications = new ArrayList<>();
    public static ArrayList<Component> scoreList = new ArrayList<>();
}
