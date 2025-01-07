package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.RubyText;
import net.minecraft.client.font.FontStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontStorage.class)
public class MixinFontStorage {


    @Inject(method = "findGlyph(I)Lnet/minecraft/client/font/FontStorage$GlyphPair;", at = @At("HEAD"), cancellable = true)
    public void onFindGlyph(int codePoint, CallbackInfoReturnable<?> cir) {
        if (codePoint == RubyText.RUBY_MARKER) {
        }
    }

}
