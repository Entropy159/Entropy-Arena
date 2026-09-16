package dev.entropy159.arena.api.data;

import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.api.gamemode.ArenaGamemode;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.function.Function;

public class SidedDataUtils {
    public static <T> T getSided(Function<ArenaData, T> server, T client) {
        var currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) {
            return client;
        }
        return server.apply(ArenaData.get(currentServer));
    }

    public static boolean running() {
        return getSided(data -> data.running, ClientData.running);
    }

    public static boolean lobby() {
        return getSided(data -> data.lobby, ClientData.inLobby);
    }

    public static int timer() {
        return getSided(data -> data.timer, ClientData.timer);
    }

    public static ArenaGamemode gamemode() {
        return getSided(data -> data.currentGamemode, ClientData.currentGamemode);
    }
}
