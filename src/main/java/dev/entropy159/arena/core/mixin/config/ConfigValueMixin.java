package dev.entropy159.arena.core.mixin.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.entropy159.arena.api.client.ClientData;
import dev.entropy159.arena.api.data.ArenaData;
import dev.entropy159.arena.api.map.ArenaMap;
import dev.entropy159.arena.core.mixininterfaces.ConfigValueAddon;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ModConfigSpec.ConfigValue.class)
public abstract class ConfigValueMixin<T> implements ConfigValueAddon<T> {
    @Shadow
    private @Nullable T cachedValue;

    @Shadow
    public abstract T getRaw();

    @Shadow
    @Final
    private List<String> path;

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private T perMap(T original) {
        String modID = entropylib$getModID();
        if (modID == null) {
            return original;
        }
        switch (FMLEnvironment.dist) {
            case CLIENT -> {
                CommentedConfig config = ClientData.configOverrides.get(modID);
                if (config == null) {
                    return original;
                }
                T value = config.get(path);
                return value == null ? original : value;
            }
            case DEDICATED_SERVER -> {
                MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                if (server == null) {
                    return original;
                }
                ArenaMap map = ArenaData.get(server).currentMap;
                if (map == null) {
                    return original;
                }
                T value = map.getConfigValue(path, modID);
                return value == null ? original : value;
            }
        }
        return original;
    }

    @Override
    public T entropyArena$getNormal() {
        if (cachedValue == null) {
            cachedValue = getRaw();
        }
        return cachedValue;
    }
}
