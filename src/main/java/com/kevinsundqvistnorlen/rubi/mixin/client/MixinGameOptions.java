package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.option.RubyMode;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class MixinGameOptions {

    @Inject(method = "accept", at = @At("HEAD"))
    private void injectAccept(GameOptions.Visitor visitor, CallbackInfo info) {
        RubyMode.accept(visitor);
    }
}
