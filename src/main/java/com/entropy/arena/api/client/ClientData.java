package com.entropy.arena.api.client;

import com.entropy.arena.api.Notification;
import com.entropy.arena.api.gamemode.ArenaGamemode;
import com.entropy.arena.core.map.ArenaMapInfo;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class ClientData {
    public static boolean running = false;
    public static boolean inLobby = true;
    public static int timer = 0;
    public static String currentMap;
    public static ArenaGamemode currentGamemode;

    public static ArrayList<ArenaMapInfo> votableMaps = new ArrayList<>();
    public static ArrayList<Notification> notifications = new ArrayList<>();
    public static ArrayList<Component> scoreList = new ArrayList<>();
}
