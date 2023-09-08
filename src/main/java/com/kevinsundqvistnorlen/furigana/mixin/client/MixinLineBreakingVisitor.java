package com.kevinsundqvistnorlen.furigana.mixin.client;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Pattern;

@Pseudo
@Mixin(TextHandler.LineBreakingVisitor.class)
public abstract class MixinLineBreakingVisitor {

    @Shadow
    private int lastSpaceBreak;

    @Shadow
    private Style lastSpaceStyle;

    @Shadow
    private int count;

    @Shadow
    private int startOffset;

    @Unique
    private boolean furigana = false;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    public void injectAccept(int index, Style style, int codePoint, CallbackInfoReturnable<? super Boolean> info) {
        boolean discard = switch (codePoint) {
            case '\ue9c1' -> {
                this.furigana = true;
                yield true;
            }
            case '\ue9c2' -> {
                this.furigana = false;
                this.lastSpaceBreak = this.startOffset + index + 1;
                this.lastSpaceStyle = style;
                yield true;
            }
            default -> false;
        };

        if (discard || this.furigana) {
            this.count += Character.charCount(codePoint);
            info.setReturnValue(true);
        }
    }

    @Inject(method = "accept", at = @At("TAIL"))
    public void injectAcceptTail(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> info) {
        if (Pattern.matches("\\P{L}", String.valueOf(Character.toChars(codePoint)))) {
            this.lastSpaceBreak = this.startOffset + index + 1;
            this.lastSpaceStyle = style;
        }
    }
}
