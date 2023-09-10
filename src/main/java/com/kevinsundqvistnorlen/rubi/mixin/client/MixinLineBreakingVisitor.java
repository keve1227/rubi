package com.kevinsundqvistnorlen.rubi.mixin.client;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private boolean ruby = false;

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    public void injectAccept(int index, Style style, int codePoint, CallbackInfoReturnable<? super Boolean> info) {
        boolean discard = switch (codePoint) {
            case '\ue9c0' -> {
                this.lastSpaceBreak = this.startOffset + index - 1;
                this.lastSpaceStyle = style;
                yield false;
            }

            case '\ue9c1' -> {
                this.ruby = true;
                yield true;
            }

            case '\ue9c2' -> {
                this.ruby = false;
                this.lastSpaceBreak = this.startOffset + index + 1;
                this.lastSpaceStyle = style;
                yield true;
            }

            default -> false;
        };

        if (discard || this.ruby) {
            this.count += Character.charCount(codePoint);
            info.setReturnValue(true);
        }
    }
}
