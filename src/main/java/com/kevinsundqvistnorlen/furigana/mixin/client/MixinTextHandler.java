package com.kevinsundqvistnorlen.furigana.mixin.client;

import com.kevinsundqvistnorlen.furigana.*;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextHandler.class)
public abstract class MixinTextHandler {

    @Shadow
    public abstract float getWidth(OrderedText text);

    @Inject(method = "getWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true)
    public void injectGetWidth(String text, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(this.getWidth(Utils.orderedFrom(text)));
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)F", at = @At("HEAD"), cancellable = true)
    public void injectGetWidth(StringVisitable text, CallbackInfoReturnable<Float> info) {
        info.setReturnValue(this.getWidth(Utils.orderedFrom(text)));
    }

    @ModifyVariable(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), argsOnly = true)
    public OrderedText modifyGetWidthText(OrderedText text) {
        return FuriganaText.strip(text);
    }
}
