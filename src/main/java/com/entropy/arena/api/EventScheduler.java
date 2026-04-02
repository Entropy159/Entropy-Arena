package com.entropy.arena.api;

import com.entropy.arena.core.EntropyArena;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = EntropyArena.MODID)
public class EventScheduler {
    private static final ArrayList<TickEvent> events = new ArrayList<>();

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Pre event) {
        Set<TickEvent> toRemove = new HashSet<>();
        events.forEach(runnable -> {
            if (runnable.onTick()) {
                toRemove.add(runnable);
            }
        });
        events.removeAll(toRemove);
    }

    public static void schedule(int delay, Runnable runnable) {
        events.add(new TickEvent(delay, runnable));
    }

    private static class TickEvent {
        private final Runnable runnable;
        private int tickDelay;

        public TickEvent(int tickDelay, Runnable runnable) {
            this.runnable = runnable;
            this.tickDelay = tickDelay;
        }

        public boolean onTick() {
            tickDelay--;
            if (tickDelay <= 0) {
                runnable.run();
                return true;
            }
            return false;
        }
    }
}
