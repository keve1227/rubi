package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.RubyText;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Style.class)
public abstract class MixinStyle implements IRubyStyle {
    @Final @Shadow TextColor color;
    @Final @Shadow Boolean bold;
    @Final @Shadow Boolean italic;
    @Final @Shadow Boolean underlined;
    @Final @Shadow Boolean strikethrough;
    @Final @Shadow Boolean obfuscated;
    @Final @Shadow ClickEvent clickEvent;
    @Final @Shadow HoverEvent hoverEvent;
    @Final @Shadow String insertion;
    @Final @Shadow Identifier font;
    @Unique private @Nullable RubyText ruby;

    @Invoker("<init>")
    private static @NotNull Style invokeConstructor(
        @Nullable TextColor color,
        @Nullable Boolean bold,
        @Nullable Boolean italic,
        @Nullable Boolean underlined,
        @Nullable Boolean strikethrough,
        @Nullable Boolean obfuscated,
        @Nullable ClickEvent clickEvent,
        @Nullable HoverEvent hoverEvent,
        @Nullable String insertion,
        @Nullable Identifier font
    ) {
        return Style.EMPTY;
    }

    @Shadow
    public abstract boolean equals(Object o);

    @Override
    public Style rubi$withRuby(String word, String ruby) {
        var rubyText = new RubyText(word, ruby, (Style) (Object) this);
        var result = MixinStyle.invokeConstructor(
            this.color,
            this.bold,
            this.italic,
            this.underlined,
            this.strikethrough,
            this.obfuscated,
            this.clickEvent,
            this.hoverEvent,
            this.insertion,
            this.font
        );
        //noinspection DataFlowIssue
        ((MixinStyle) (Object) result).setRuby(rubyText);
        return result;
    }

    @Override
    public @Nullable RubyText rubi$getRuby() {
        return this.ruby;
    }

    @Unique
    private void setRuby(@Nullable RubyText ruby) {
        this.ruby = ruby;
    }

    @Inject(method = "withParent", at = @At("RETURN"))
    public void onWithParent(Style parent, CallbackInfoReturnable<Style> cir) {
        if (cir.getReturnValue() == parent) return;
        if (IRubyStyle.getRuby(parent).isEmpty()) return;
        ((MixinStyle) (Object) cir.getReturnValue()).setRuby(((IRubyStyle) parent).rubi$getRuby());
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    public void onEquals(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            cir.setReturnValue(Objects.equals(this.ruby, ((IRubyStyle) o).rubi$getRuby()));
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    public void onHashCode(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Objects.hash(cir.getReturnValue(), this.ruby));
    }
}
