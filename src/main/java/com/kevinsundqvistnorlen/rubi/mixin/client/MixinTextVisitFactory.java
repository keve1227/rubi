package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.RubyText;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextVisitFactory.class)
public abstract class MixinTextVisitFactory {
    @Inject(
        method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;" +
            "Lnet/minecraft/text/CharacterVisitor;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Formatting;byCode(C)Lnet/minecraft/util/Formatting;"),
        cancellable = true
    )
    private static void onFormattingCode(
        String text, int startIndex, Style startingStyle, Style resetStyle, CharacterVisitor visitor,
        CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 2) Style style, @Local(ordinal = 2) LocalIntRef index,
        @Local(ordinal = 1) char formattingCode
    ) {
        if (formattingCode == '^') {
            var matcher = RubyText.RUBY_PATTERN.matcher(text).region(index.get(), text.length());
            if (matcher.lookingAt()) {
                var rubyText = RubyText.fromFormatted(matcher.group(1), matcher.group(2), style);
                var rubyStyle = ((IRubyStyle) style).rubi$withRuby(rubyText);
                if (!visitor.accept(index.get(), rubyStyle, '￼')) {
                    cir.setReturnValue(false);
                    return;
                }

                index.set(matcher.end() - 2);
            }
        }
    }
}
