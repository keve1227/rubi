package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {
    @Inject(method = "accept", at = @At("HEAD"))
    private void onAccept(GameOptions.Visitor visitor, CallbackInfo info) {
        RubyRenderMode.accept(visitor);
    }
}
