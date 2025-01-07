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

    @ModifyVariable(method = "<init>", argsOnly = true, at = @At("LOAD"))
    private static TextHandler.WidthRetriever modifyWidthRetrieverConstructor(TextHandler.WidthRetriever widthRetriever) {
        return (codePoint, style) ->
            codePoint == RubyText.RUBY_MARKER && ((IRubyStyle) style).getRuby() != null ?
                ((IRubyStyle) style).getRuby().getWidth(widthRetriever, style) :
                widthRetriever.getWidth(codePoint, style);
    }
}
