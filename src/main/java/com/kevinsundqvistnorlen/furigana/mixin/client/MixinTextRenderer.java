package com.kevinsundqvistnorlen.furigana.mixin.client;

import com.kevinsundqvistnorlen.furigana.FuriganaText;
import com.kevinsundqvistnorlen.furigana.Utils;
import java.util.List;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Math;
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
    public abstract int drawInternal(
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
        info.setReturnValue(customDraw(FuriganaText.parseCached(text), x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light));
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
        outlineColor = invokeTweakTransparency(outlineColor);
        List<FuriganaText> texts = FuriganaText.parseCached(text);

        for (int i = 0; i < 8; i++) {
            float a = (i / 4.0f) * (float) Math.PI;
            float ox = Math.cos(a);
            float oy = Math.sin(a);

            customDraw(texts, x + ox * 0.5f, y + oy * 0.5f, outlineColor, false, matrix, vertexConsumers, TextLayerType.NORMAL, 0, light);
        }

        customDraw(texts, x, y, color, false, matrix, vertexConsumers, TextLayerType.POLYGON_OFFSET, 0, light);
        info.cancel();
    }

    public int customDraw(
        List<FuriganaText> texts,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light
    ) {
        for (final var text : texts) {
            float textWidth = handler.getWidth(text);
            var furigana = Utils.style(text.getFurigana(), style -> style.withBold(true));

            if (furigana != null) {
                final float heightScale = 0.5f;

                float furiganaWidth = handler.getWidth(furigana);
                float scale = Math.min(heightScale, textWidth / furiganaWidth);

                float lineHeight = fontHeight * heightScale;
                float xx = x + (textWidth - furiganaWidth * scale) / 2;
                float yy = y - lineHeight;

                drawInternal(
                    furigana,
                    xx,
                    yy,
                    color,
                    shadow,
                    new Matrix4f(matrix).scaleAround(scale, heightScale, 1, xx, yy + lineHeight, 0),
                    vertexConsumers,
                    layerType,
                    backgroundColor,
                    light
                );

                drawInternal(
                    text,
                    x,
                    y,
                    color,
                    shadow,
                    new Matrix4f(matrix).scaleAround(1, 0.8f, 1, x + textWidth / 2, y + fontHeight, 0),
                    vertexConsumers,
                    layerType,
                    backgroundColor,
                    light
                );
            } else {
                drawInternal(text, x, y, color, shadow, matrix, vertexConsumers, layerType, backgroundColor, light);
            }

            x += textWidth;
        }

        return (int) Math.floor(x);
    }
}
