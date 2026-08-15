package dev.entropy159.arena.api.client;

import com.electronwill.nightconfig.core.CommentedConfig;
import dev.entropy159.arena.api.gamemode.ArenaGamemode;
import dev.entropy159.arena.api.map.ArenaMapInfo;
import dev.entropy159.arena.api.util.ArenaGameType;
import dev.entropy159.arena.api.util.Notification;
import dev.entropy159.arena.client.PingIcon;
import net.minecraft.network.chat.Component;

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
    public static Map<String, CommentedConfig> configOverrides = new HashMap<>();
}
