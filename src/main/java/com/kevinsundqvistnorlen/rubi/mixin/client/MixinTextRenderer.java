package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.TextDrawer;
import com.kevinsundqvistnorlen.rubi.Utils;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import org.joml.Math;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer {
    @Final @Shadow public int fontHeight;
    @Final @Shadow private TextHandler handler;
    @Unique private boolean isSeparatingDrawCall = false;

    @Shadow
    public abstract int draw(
        OrderedText text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextRenderer.TextLayerType layerType,
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

    @Redirect(
        method = "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;" +
            "Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/font/TextRenderer;drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;" +
            "Lnet/minecraft/client/render/VertexConsumerProvider;" +
            "Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"
    )
    )
    public int redirectDraw(
        TextRenderer textRenderer,
        String text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextRenderer.TextLayerType layerType,
        int backgroundColor,
        int light,
        boolean reverse
    ) {
        return this.draw(
            Utils.orderedFrom(text),
            x,
            y,
            color,
            shadow,
            matrix,
            vertexConsumers,
            layerType,
            backgroundColor,
            light
        );
    }

    @Inject(
        method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;" +
            "Lnet/minecraft/client/render/VertexConsumerProvider;" +
            "Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I", at = @At("HEAD"), cancellable = true
    )
    public void injectDraw(
        OrderedText text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextRenderer.TextLayerType layerType,
        int backgroundColor,
        int light,
        CallbackInfoReturnable<Integer> info
    ) {
        if (!this.isSeparatingDrawCall) {
            this.isSeparatingDrawCall = true;
            int advance =
                TextDrawer.draw(text, x, y, matrix, this.handler, this.fontHeight, (t, xx, yy, m) -> this.draw(
                    t,
                    Math.round(xx),
                    Math.round(yy),
                    color,
                    shadow,
                    m,
                    vertexConsumers,
                    layerType,
                    backgroundColor,
                    light
                ));
            this.isSeparatingDrawCall = false;
            info.setReturnValue(advance);
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
        if (!this.isSeparatingDrawCall) {
            this.isSeparatingDrawCall = true;
            TextDrawer.draw(
                text,
                x,
                y,
                matrix,
                this.handler,
                this.fontHeight,
                (t, xx, yy, m) -> this.drawWithOutline(t, xx, yy, color, outlineColor, m, vertexConsumers, light)
            );
            info.cancel();
            this.isSeparatingDrawCall = false;
        }
    }
}
