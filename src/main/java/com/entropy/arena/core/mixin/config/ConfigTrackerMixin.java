package com.entropy.arena.core.mixin.config;

import com.entropy.arena.core.EntropyArena;
import com.entropy.arena.core.mixininterfaces.ConfigValueAddon;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConfigTracker.class)
public class ConfigTrackerMixin {
    @Inject(method = "registerConfig(Lnet/neoforged/fml/config/ModConfig$Type;Lnet/neoforged/fml/config/IConfigSpec;Lnet/neoforged/fml/ModContainer;Ljava/lang/String;)Lnet/neoforged/fml/config/ModConfig;", at = @At("HEAD"))
    private void addModID(ModConfig.Type type, IConfigSpec spec, ModContainer container, String fileName, CallbackInfoReturnable<ModConfig> cir) {
        EntropyArena.LOGGER.info("Adding mod ID!");
        if (spec instanceof ModConfigSpec modSpec) {
            modSpec.getValues().entrySet().forEach(entry -> {
                if (entry.getRawValue() instanceof ConfigValueAddon<?> addon) {
                    addon.entropyArena$setModID(container.getModId());
                }
            });
        }
    }
}
