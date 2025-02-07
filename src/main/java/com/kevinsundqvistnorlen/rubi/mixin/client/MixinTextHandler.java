package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.OrderedTextStringVisitable;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(TextHandler.class)
public abstract class MixinTextHandler {
    @Unique private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

    @ModifyVariable(
        method = "<init>",
        argsOnly = true,
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/font/TextHandler;" +
                "widthRetriever:Lnet/minecraft/client/font/TextHandler$WidthRetriever;"
        )
    )
    private TextHandler.WidthRetriever modifyWidthRetrieverConstructor(
        TextHandler.WidthRetriever widthRetriever
    ) {
        return (codePoint, style) -> IRubyStyle
            .getRuby(style)
            .map((rubyText -> rubyText.getWidth((TextHandler) (Object) this)))
            .orElseGet(() -> widthRetriever.getWidth(codePoint, style));
    }

    @Shadow
    public abstract float getWidth(StringVisitable text);

    @Inject(
        method = "getWidth(Lnet/minecraft/text/StringVisitable;)F",
        at = @At("HEAD"),
        cancellable = true,
        order = 900
    )
    public void onGetWidth(StringVisitable text, CallbackInfoReturnable<Float> cir) {
        if (this.recursionGuard.get()) return;
        this.recursionGuard.set(true);

        try {
            var width = new MutableFloat();
            text.visit((style, string) -> {
                width.add(
                    IRubyStyle
                        .getRuby(style)
                        .map(ruby -> ruby.getWidth((TextHandler) (Object) this))
                        .orElseGet(() -> this.getWidth(StringVisitable.styled(string, style)))
                );
                return Optional.empty();
            }, Style.EMPTY);
            cir.setReturnValue(width.floatValue());
        } finally {
            this.recursionGuard.set(false);
        }
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true, order = 900)
    private void onGetWidth(OrderedText text, CallbackInfoReturnable<Float> cir) {
        this.onGetWidth(new OrderedTextStringVisitable(text), cir);
    }
}
