package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextHandler.class)
public abstract class MixinTextHandler {
    @Unique private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

    @Inject(method = "<init>", at = @At("CTOR_HEAD"))
    private void onTextHandlerInit(
        TextHandler.WidthRetriever widthRetriever, CallbackInfo ci,
        @Local(argsOnly = true) LocalRef<TextHandler.WidthRetriever> localWidthRetriever
    ) {
        localWidthRetriever.set((codePoint, style) -> IRubyStyle
            .getRuby(style)
            .map(rubyText -> rubyText.getWidth((TextHandler) (Object) this))
            .orElseGet(() -> widthRetriever.getWidth(codePoint, style)));
    }

    @Shadow
    public abstract float getWidth(StringVisitable text);

    @Unique
    public abstract float hello();

    @Shadow
    public abstract float getWidth(OrderedText text);

    @Inject(method = "getWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true, order = 900)
    private void onGetWidth(String text, CallbackInfoReturnable<Float> cir) {
        this.onGetWidth(visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor), cir);
    }

    @Inject(
        method = "getWidth(Lnet/minecraft/text/StringVisitable;)F", at = @At("HEAD"), cancellable = true, order = 900
    )
    private void onGetWidth(StringVisitable text, CallbackInfoReturnable<Float> cir) {
        this.onGetWidth(visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor), cir);
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true, order = 900)
    private void onGetWidth(OrderedText text, CallbackInfoReturnable<Float> cir) {
        if (this.recursionGuard.get()) return;
        this.recursionGuard.set(true);

        try {
            var width = new MutableFloat();
            text.accept((index, style, codePoint) -> {
                width.add(
                    IRubyStyle
                        .getRuby(style)
                        .map(ruby -> ruby.getWidth((TextHandler) (Object) this))
                        .orElseGet(() -> this.getWidth(OrderedText.styled(codePoint, style)))
                );
                return true;
            });
            cir.setReturnValue(width.floatValue());
        } finally {
            this.recursionGuard.set(false);
        }
    }
}
