package com.entropy.arena.api;

import com.entropy.arena.core.EntropyArena;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;

@EventBusSubscriber(modid = EntropyArena.MODID)
public class EventScheduler {
    private static final ArrayList<TickEvent> eventList = new ArrayList<>();

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Pre event) {
        ArrayList<TickEvent> events = new ArrayList<>(eventList);
        for (TickEvent runnable : events) {
            if (runnable.onTick()) {
                eventList.remove(runnable);
            }
        }
    }

    public static void schedule(int delay, Runnable runnable) {
        eventList.add(new TickEvent(delay, runnable));
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
