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

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true)
    public void injectGetWidth(OrderedText text, CallbackInfoReturnable<Float> info) {
        var parsed = FuriganaText.cachedParse(text);
        if (!parsed.hasFurigana()) return;

        final FuriganaMode mode = FuriganaMode.getValue();

        float width = 0;
        for (final var part : parsed.texts()) {
            if (part.getClass() == FuriganaText.class) {
                width += switch (mode) {
                    case ABOVE -> ((FuriganaText) part).getWidth((TextHandler) (Object) this);
                    case REPLACE -> this.getWidth(((FuriganaText) part).furigana());
                    case HIDDEN -> this.getWidth(((FuriganaText) part).text());
                };
            } else {
                width += this.getWidth(part);
            }
        }

        info.setReturnValue(width);
    }
}
