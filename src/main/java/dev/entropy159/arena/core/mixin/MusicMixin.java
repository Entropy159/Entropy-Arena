package dev.entropy159.arena.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.entropy159.arena.api.client.ClientData;
import net.minecraft.sounds.Music;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Music.class)
public class MusicMixin {
    @ModifyReturnValue(method = "getMinDelay", at = @At("RETURN"))
    private int min(int original) {
        if (ClientData.running) {
            return 3;
        }
        return original;
    }

    @ModifyReturnValue(method = "getMaxDelay", at = @At("RETURN"))
    private int max(int original) {
        if (ClientData.running) {
            return 10;
        }
        return original;
    }
}
