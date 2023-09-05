package com.kevinsundqvistnorlen.furigana.mixin.client;

import com.kevinsundqvistnorlen.furigana.FuriganaText;
import com.kevinsundqvistnorlen.furigana.FuriganaText.FuriganaParseResult;
import com.kevinsundqvistnorlen.furigana.Utils;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer {

    @Shadow
    public int fontHeight;

    @Shadow
    public TextHandler handler;

    @Shadow
    public abstract int draw(
        OrderedText text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light
    );

    @Shadow
    public abstract void drawWithOutline(
        OrderedText text,
        float x,
        float y,
        int color,
        int outlineColor,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        int light
    );

    @Invoker("tweakTransparency")
    public static int invokeTweakTransparency(int color) {
        throw new AssertionError();
    }

    @Redirect(
        method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I",
        at = @At("INVOKE"),
        expect = 1
    )
    public int redirectDrawString(
        TextRenderer textRenderer,
        String text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light,
        boolean reverse
    ) {
        return draw(Utils.orderedFrom(text), x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Inject(
        method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
        at = @At("HEAD"),
        cancellable = true
    )
    public void injectDraw(
        OrderedText text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light,
        CallbackInfoReturnable<Integer> info
    ) {
        FuriganaParseResult parsed = FuriganaText.parseCached(text);

        if (parsed.hasFurigana()) {
            info.setReturnValue(
                Utils.drawFuriganaTexts(
                    parsed.texts(),
                    x,
                    y,
                    matrix,
                    handler,
                    fontHeight,
                    (t, xx, yy, m) -> {
                        draw(t, xx, yy, color, shadow, m, vertexConsumers, layerType, backgroundColor, light);
                    }
                )
            );
        }
    }

    @Inject(method = "drawWithOutline", at = @At("HEAD"), cancellable = true)
    public void injectDrawWithOutline(
        OrderedText text,
        float x,
        float y,
        int color,
        int outlineColor,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        int light,
        CallbackInfo info
    ) {
        FuriganaParseResult parsed = FuriganaText.parseCached(text);

        if (parsed.hasFurigana()) {
            Utils.drawFuriganaTexts(
                parsed.texts(),
                x,
                y,
                matrix,
                handler,
                fontHeight,
                (t, xx, yy, m) -> {
                    drawWithOutline(t, xx, yy, color, outlineColor, m, vertexConsumers, light);
                }
            );

            info.cancel();
        }
    }
}
