package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.RubyText;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringVisitable.class)
public interface MixinStringVisitable {
    @Inject(method = "getString", at = @At("RETURN"), cancellable = true)
    default void onGetString(CallbackInfoReturnable<String> info) {
        info.setReturnValue(RubyText.strip(info.getReturnValue()));
    }
}
