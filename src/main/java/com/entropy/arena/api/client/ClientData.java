package com.entropy.arena.api.client;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.api.map.ArenaMapInfo;
import com.entropy.arena.api.util.ArenaGameType;
import com.entropy.arena.api.util.Notification;
import com.entropy.arena.client.PingIcon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

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
    public static CopyOnWriteArraySet<PingIcon> pings = new CopyOnWriteArraySet<>();
    public static HashMap<Integer, Vec3> entitiesToUnlerp = new HashMap<>();
    public static Map<String, CommentedConfig> configOverrides = new HashMap<>();
}
