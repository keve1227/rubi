package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.*;
import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
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
    public void injectGetWidth(String text, CallbackInfoReturnable<? super Float> info) {
        info.setReturnValue(this.getWidth(Utils.orderedFrom(text)));
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)F", at = @At("HEAD"), cancellable = true)
    public void injectGetWidth(StringVisitable text, CallbackInfoReturnable<? super Float> info) {
        info.setReturnValue(this.getWidth(Utils.orderedFrom(text)));
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true)
    public void injectGetWidth(OrderedText text, CallbackInfoReturnable<? super Float> info) {
        var parsed = RubyText.cachedParse(text);
        if (!parsed.hasRuby()) return;

        final RubyRenderMode mode = RubyRenderMode.getValue();

        float width = 0;
        for (final var part : parsed.texts()) {
            if (part.getClass() == RubyText.class) {
                width += switch (mode) {
                    case ABOVE, BELOW -> ((RubyText) part).getWidth((TextHandler) (Object) this);
                    case REPLACE -> this.getWidth(((RubyText) part).ruby());
                    case HIDDEN -> this.getWidth(((RubyText) part).text());
                };
            } else {
                width += this.getWidth(part);
            }
        }

        info.setReturnValue(width);
    }
}
