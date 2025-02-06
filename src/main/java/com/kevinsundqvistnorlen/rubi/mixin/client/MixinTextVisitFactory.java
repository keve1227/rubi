package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.RubyText;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.text.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory {
    @Inject(
        method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;" +
            "Lnet/minecraft/text/CharacterVisitor;)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Formatting;byCode(C)Lnet/minecraft/util/Formatting;"),
        cancellable = true
    )
    private static void onFormattingCode(
        String text,
        int startIndex,
        Style startingStyle,
        Style resetStyle,
        CharacterVisitor visitor,
        CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 2) Style style,
        @Local(ordinal = 2) int index,
        @Local(ordinal = 1) char styleCode
    ) {
        if (styleCode == '^') {
            var matcher = RubyText.RUBY_PATTERN.matcher(text);
            if (matcher.find(index)) {
                final var word = matcher.group(1);
                final var ruby = matcher.group(2);
                if (!visitor.accept(index, ((IRubyStyle) style).rubi$withRuby(word, ruby), '￼')) {
                    cir.setReturnValue(false);
                    return;
                }
                index = matcher.end();
                cir.setReturnValue(TextVisitFactory.visitFormatted(text, index, style, visitor));
            }
        }
    }
}
