package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.*;
import com.kevinsundqvistnorlen.rubi.option.ITextHandler;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextHandler.class)
public abstract class MixinTextHandler implements ITextHandler {

    @Unique
    private boolean getWidthMutex = false;

    @Shadow
    public float getWidth(OrderedText text) { return 0f; }

    @Inject(method = "getWidth(Ljava/lang/String;)F", at = @At("HEAD"), cancellable = true, order = 900)
    public void injectGetWidth(String text, CallbackInfoReturnable<Float> info) {
        onGetWidthOrderedText(Utils.orderedFrom(text), info);
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/StringVisitable;)F", at = @At("HEAD"), cancellable = true, order = 900)
    public void injectGetWidth(StringVisitable text, CallbackInfoReturnable<Float> info) {
        onGetWidthOrderedText(Utils.orderedFrom(text), info);
    }

    @Inject(method = "getWidth(Lnet/minecraft/text/OrderedText;)F", at = @At("HEAD"), cancellable = true, order = 900)
    private void onGetWidthOrderedText(OrderedText text, CallbackInfoReturnable<Float> cir) {
        if (!getWidthMutex) {
            getWidthMutex = true;
            MutableFloat width = new MutableFloat();
            Utils.splitWithDelimitersOrderedText(text, RubyText.RUBY_MARKER, orderedText -> {
                if (orderedText == OrderedText.EMPTY) return;
                width.add(this.getWidth(orderedText));
            }, orderedText -> {
                final MutableObject<RubyText> rubyTextWrapper = new MutableObject<>(null);
                final MutableObject<Style> styleWrapper = new MutableObject<>(null);
                orderedText.accept((index, style, codePoint) -> {
                    if (rubyTextWrapper.getValue() != null) throw new IllegalStateException("Expected only one element in OrderedText rubiCharacter");
                    if (codePoint != RubyText.RUBY_MARKER || IRubyStyle.getRuby(style) == null)
                        throw new IllegalArgumentException("Expected rubi character");
                    rubyTextWrapper.setValue(IRubyStyle.getRuby(style));
                    styleWrapper.setValue(style);
                    return true;
                });
                width.add(rubyTextWrapper.getValue().getWidth(this, styleWrapper.getValue()));
            });
            cir.setReturnValue(width.floatValue());
            getWidthMutex = false;
        }
    }

    @ModifyVariable(method = "<init>", argsOnly = true, at = @At("LOAD"))
    private static TextHandler.WidthRetriever modifyWidthRetrieverConstructor(TextHandler.WidthRetriever widthRetriever) {
        return (codePoint, style) ->
            codePoint == RubyText.RUBY_MARKER && ((IRubyStyle) style).getRuby() != null ?
                ((IRubyStyle) style).getRuby().getWidth(widthRetriever, style) :
                widthRetriever.getWidth(codePoint, style);
    }
}
