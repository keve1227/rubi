package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.RubyText;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;

@Mixin(Style.class)
public abstract class MixinStyle implements IRubyStyle {

    @Shadow
    public abstract boolean equals(Object o);
    @Final
    @Shadow
    TextColor color;
    @Final
    @Shadow
    Boolean bold;
    @Final
    @Shadow
    Boolean italic;
    @Final
    @Shadow
    Boolean underlined;
    @Final
    @Shadow
    Boolean strikethrough;
    @Final
    @Shadow
    Boolean obfuscated;
    @Final
    @Shadow
    ClickEvent clickEvent;
    @Final
    @Shadow
    HoverEvent hoverEvent;
    @Final
    @Shadow
    String insertion;
    @Final
    @Shadow
    Identifier font;

    @Unique
    private @Nullable RubyText ruby;

    @Shadow
    private static Style of(
        Optional<TextColor> color,
        Optional<Boolean> bold,
        Optional<Boolean> italic,
        Optional<Boolean> underlined,
        Optional<Boolean> strikethrough,
        Optional<Boolean> obfuscated,
        Optional<ClickEvent> optional,
        Optional<HoverEvent> optional2,
        Optional<String> optional3,
        Optional<Identifier> optional4
    ) {return Style.EMPTY;}

    @Override
    public Style withRuby(String word, String ruby) {
        var newRuby = new RubyText(word, ruby);
        var result = of(
            Optional.ofNullable(this.color),
            Optional.ofNullable(this.bold),
            Optional.ofNullable(this.italic),
            Optional.ofNullable(this.underlined),
            Optional.ofNullable(this.strikethrough),
            Optional.ofNullable(this.obfuscated),
            Optional.ofNullable(this.clickEvent),
            Optional.ofNullable(this.hoverEvent),
            Optional.ofNullable(this.insertion),
            Optional.ofNullable(this.font)
        );
        ((MixinStyle) (Object) result).ruby = newRuby;
        return result;
    }

    @Override
    public @Nullable RubyText getRuby() {
        return this.ruby;
    }

    @Override
    public Style removeRuby() {
        var result = of(
            Optional.ofNullable(this.color),
            Optional.ofNullable(this.bold),
            Optional.ofNullable(this.italic),
            Optional.ofNullable(this.underlined),
            Optional.ofNullable(this.strikethrough),
            Optional.ofNullable(this.obfuscated),
            Optional.ofNullable(this.clickEvent),
            Optional.ofNullable(this.hoverEvent),
            Optional.ofNullable(this.insertion),
            Optional.ofNullable(this.font)
        );
        ((MixinStyle) (Object) result).ruby = null;
        return result;
    }

    @Inject(method = "withParent", at = @At("RETURN"))
    public void onWithParent(Style parent, CallbackInfoReturnable<Style> cir) {
        if (cir.getReturnValue() == parent) return;
        if (((IRubyStyle) parent).getRuby() == null) return;
        ((MixinStyle) (Object) cir.getReturnValue()).ruby = ((MixinStyle) (Object) parent).ruby;
    }

    @Inject(method = "equals", at = @At("RETURN"), cancellable = true)
    public void onEquals(Object o, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) cir.setReturnValue(Objects.equals(this.getRuby(), ((IRubyStyle) o).getRuby()));
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    public void onHashCode(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Objects.hash(cir.getReturnValue(), this.ruby));
    }
}
