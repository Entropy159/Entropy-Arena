package dev.entropy159.arena.api.gamemode;

import dev.entropy159.arena.api.util.ArenaGameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public abstract class LivesGamemode extends FFAGamemode {
    public LivesGamemode(ResourceLocation id) {
        super(id);
    }

    @Override
    public int defaultScore(ServerPlayer player) {
        return getLifeCount();
    }

    public abstract int getLifeCount();

    public int targetWinners() {
        return 1;
    }

    public boolean scoreShouldEnd() {
        return scoreMap.values().stream().filter(i -> i < 0).count() <= targetWinners();
    }

    @Override
    public void onDeath(ServerPlayer player, DamageSource source) {
        super.onDeath(player, source);
        setScore(player, -1);
    }

    @Override
    public boolean shouldWin(ServerLevel level, ArenaGameType type, int timer, int targetScore) {
        return type.isTimed() ? super.shouldWin(level, type, timer, targetScore) : scoreShouldEnd();
    }

    @Override
    public void selfDeath(ServerPlayer player) {

    }
}
