package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.Utils;
import com.kevinsundqvistnorlen.rubi.option.ITextHandler;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextHandler.class)
public abstract class MixinTextHandler implements ITextHandler {
    @Shadow @Final TextHandler.WidthRetriever widthRetriever;
    @Unique private volatile boolean getWidthMutex = false;

    @ModifyVariable(method = "<init>", argsOnly = true, at = @At("LOAD"))
    private static TextHandler.WidthRetriever modifyWidthRetrieverConstructor(
        TextHandler.WidthRetriever widthRetriever
    ) {
        return (codePoint, style) -> IRubyStyle
            .getRuby(style)
            .map((rubyText -> rubyText.getWidth(widthRetriever)))
            .orElseGet(() -> widthRetriever.getWidth(codePoint, style));
    }

    @Shadow
    public float getWidth(OrderedText text) {
        return 0f;
    }

    @Inject(
        method = "getWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true, order = 900
    )
    public void injectGetWidth(String text, CallbackInfoReturnable<Float> info) {
        this.onGetWidthOrderedText(Utils.orderedFrom(text), info);
    }

    @Inject(
        method = "getWidth(Lnet/minecraft/text/StringVisitable;)F", at = @At("HEAD"), cancellable = true, order = 900
    )
    public void injectGetWidth(StringVisitable text, CallbackInfoReturnable<Float> info) {
        this.onGetWidthOrderedText(Utils.orderedFrom(text), info);
    }

    @Inject(
        method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true, order = 900
    )
    private void onGetWidthOrderedText(OrderedText text, CallbackInfoReturnable<Float> cir) {
        if (!this.getWidthMutex) {
            this.getWidthMutex = true;
            MutableFloat width = new MutableFloat();
            text.accept((index, style, codePoint) -> {
                width.add(this.widthRetriever.getWidth(codePoint, style));
                return true;
            });
            cir.setReturnValue(width.floatValue());
            this.getWidthMutex = false;
        }
    }
}
