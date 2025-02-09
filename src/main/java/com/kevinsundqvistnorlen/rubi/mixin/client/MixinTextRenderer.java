package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.TextDrawer;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.*;
import org.joml.Math;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextRenderer.class)
public abstract class MixinTextRenderer {
    @Unique private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

    @Final @Shadow public int fontHeight;

    @Final @Shadow private TextHandler handler;

    @Shadow
    public abstract int draw(
        OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix,
        VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light
    );

    @Shadow
    public abstract void drawWithOutline(
        OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix,
        VertexConsumerProvider vertexConsumers, int light
    );

    @Redirect(
        method =
            "draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;" +
                "Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
        at = @At(
            value = "INVOKE",
            target =
                "Lnet/minecraft/client/font/TextRenderer;drawInternal(Ljava/lang/String;FFIZLorg/joml/Matrix4f;" +
                    "Lnet/minecraft/client/render/VertexConsumerProvider;" + "Lnet/minecraft/client/font" +
                    "/TextRenderer$TextLayerType;IIZ)I"
        )
    )
    public int redirectDraw(
        TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow, Matrix4f matrix,
        VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light,
        boolean reverse
    ) {
        return this.draw(
            visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor), x, y, color, shadow, matrix,
            vertexConsumers, layerType, backgroundColor, light
        );
    }

    @Inject(
        method = "draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;" + "Lnet/minecraft/client/render" +
            "/VertexConsumerProvider;" + "Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I",
        at = @At("HEAD"), cancellable = true
    )
    public void onDraw(
        OrderedText text, float x, float y, int color, boolean shadow, Matrix4f matrix,
        VertexConsumerProvider vertexConsumers, TextRenderer.TextLayerType layerType, int backgroundColor, int light,
        CallbackInfoReturnable<Integer> cir
    ) {
        if (this.recursionGuard.get()) return;
        this.recursionGuard.set(true);

        try {
            x = TextDrawer.draw(
                text, x, y, matrix, this.handler, this.fontHeight, (t, xx, yy, m) -> this.draw(
                    t, Math.round(xx), Math.round(yy), color, shadow, m, vertexConsumers, layerType, backgroundColor,
                    light
                )
            );
            cir.setReturnValue((int) Math.ceil(x) + (shadow ? 1 : 0));
        } finally {
            this.recursionGuard.set(false);
        }
    }

    @Inject(method = "drawWithOutline", at = @At("HEAD"), cancellable = true)
    public void onDrawWithOutline(
        OrderedText text, float x, float y, int color, int outlineColor, Matrix4f matrix,
        VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci
    ) {
        if (this.recursionGuard.get()) return;
        this.recursionGuard.set(true);

        try {
            TextDrawer.draw(
                text, x, y, matrix, this.handler, this.fontHeight,
                (t, xx, yy, m) -> this.drawWithOutline(t, xx, yy, color, outlineColor, m, vertexConsumers, light)
            );
            ci.cancel();
        } finally {
            this.recursionGuard.set(false);
        }
    }
}
