package com.entropy.arena.api;

import com.entropy.arena.core.EntropyArena;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@EventBusSubscriber(modid = EntropyArena.MODID)
public class EventScheduler {
    private static final CopyOnWriteArrayList<TickEvent> eventList = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    private static void onServerTick(ServerTickEvent.Pre event) {
        ArrayList<TickEvent> toRemove = new ArrayList<>();
        eventList.forEach(e -> {
            if (e == null || e.onTick()) {
                toRemove.add(e);
            }
        });
        eventList.removeAll(toRemove);
    }

    public static void schedule(int delay, Runnable runnable) {
        eventList.add(new TickEvent(delay, runnable));
    }

    public static void schedule(int delay, Supplier<Boolean> shouldRun, Runnable runnable) {
        eventList.add(new TickEvent(delay, runnable, shouldRun, false));
    }

    public static void scheduleUntil(int delay, Supplier<Boolean> shouldEnd, Runnable runnable) {
        eventList.add(new TickEvent(delay, runnable, shouldEnd, true));
    }

    private static class TickEvent {
        private final Runnable runnable;
        private final int tickDelay;
        private int tickCounter;
        private final Supplier<Boolean> condition;
        private final boolean inverted;

        public TickEvent(int tickDelay, Runnable runnable) {
            this(tickDelay, runnable, () -> true, false);
        }

        public TickEvent(int tickDelay, Runnable runnable, Supplier<Boolean> condition, boolean inverted) {
            this.tickDelay = tickDelay;
            this.tickCounter = tickDelay;
            this.runnable = runnable;
            this.condition = condition;
            this.inverted = inverted;
        }

        public boolean onTick() {
            tickCounter--;
            if (tickCounter <= 0) {
                if (inverted) {
                    if (condition.get()) {
                        return true;
                    }
                    runnable.run();
                    tickCounter = tickDelay;
                    return false;
                }
                if (condition.get()) {
                    runnable.run();
                    return true;
                }
                tickCounter = tickDelay;
            }
            return false;
        }
    }
}
