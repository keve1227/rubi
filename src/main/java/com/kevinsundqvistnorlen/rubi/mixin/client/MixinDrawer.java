package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.HasMatrixProp;
import com.kevinsundqvistnorlen.rubi.IRubyStyle;
import com.kevinsundqvistnorlen.rubi.RubyText;
import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.font.BakedGlyph;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(targets = "net/minecraft/client/font/TextRenderer$Drawer")
public abstract class MixinDrawer {

    @Final
    @Shadow
    TextRenderer field_24240;

    @Shadow
    float x, y;

    @Final
    @Shadow
    private Matrix4f matrix;

    @Unique
    private Matrix4f currentMatrix;

    @Inject(
        method = "<init>(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/client/render/VertexConsumerProvider;FFIIZLorg/joml/Matrix4f;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IZ)V",
        at = @At("RETURN")
    )
    public void constructor(
        TextRenderer textRenderer,
        VertexConsumerProvider vertexConsumers,
        float x,
        float y,
        int color,
        int backgroundColor,
        boolean shadow,
        Matrix4f matrix,
        TextRenderer.TextLayerType layerType,
        int light,
        boolean swapZIndex,
        CallbackInfo ci
    ) {
        this.currentMatrix = this.matrix;
    }

    @Inject(method = "accept(ILnet/minecraft/text/Style;I)Z", at = @At("HEAD"), cancellable = true)
    public void onAccept(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        if (codePoint == RubyText.RUBY_MARKER && ((IRubyStyle)style).getRuby() != null) {
            var mode = RubyRenderMode.getOption().getValue();
            var rubyBlob = ((IRubyStyle)style).getRuby();
            var rubyStyle = ((IRubyStyle)style).removeRuby();
            if (mode == RubyRenderMode.HIDDEN) {
                TextVisitFactory.visitForwards(rubyBlob.text(), rubyStyle, (CharacterVisitor) this);
                cir.setReturnValue(true);
                return;
            }
            if (mode == RubyRenderMode.REPLACE) {
                TextVisitFactory.visitForwards(rubyBlob.ruby(), rubyStyle, (CharacterVisitor) this);
                cir.setReturnValue(true);
                return;
            }
            float prevX = this.x, prevY = this.y;
            float width = field_24240.getTextHandler().getWidth(OrderedText.styled(codePoint, style));
            this.currentMatrix = new Matrix4f(this.matrix);
            this.x = 0;
            this.y = 0;
            TextVisitFactory.visitForwards(rubyBlob.text(), rubyStyle, (CharacterVisitor) this);
            float textOffset = (width - this.x * RubyText.TEXT_SCALE) / 2;
            float textX = prevX + textOffset, textY = mode == RubyRenderMode.ABOVE ? this.field_24240.fontHeight * (1 - RubyText.TEXT_SCALE) : 0;
            this.currentMatrix.scale(RubyText.TEXT_SCALE).translate(textX / RubyText.TEXT_SCALE, (prevY + textY) / RubyText.TEXT_SCALE, 0);
            this.currentMatrix = new Matrix4f(this.matrix);
            this.x = 0;
            this.y = 0;
            TextVisitFactory.visitForwards(rubyBlob.ruby(), rubyStyle.withUnderline(false).withStrikethrough(false), (CharacterVisitor) this);
            float rubyOffset = (width - this.x * RubyText.RUBY_SCALE) / 2;
            float rubyX = prevX + rubyOffset,
                  rubyY = mode == RubyRenderMode.ABOVE ?
                      textY + this.field_24240.fontHeight * (RubyText.RUBY_OVERLAP - RubyText.RUBY_SCALE) :
                      this.field_24240.fontHeight * (RubyText.TEXT_SCALE - RubyText.RUBY_OVERLAP);
            this.currentMatrix.scale(RubyText.RUBY_SCALE).translate(rubyX / RubyText.RUBY_SCALE, (prevY + rubyY) / RubyText.RUBY_SCALE, 0);
            this.currentMatrix = this.matrix;
            this.x = prevX + width;
            this.y = prevY;
            cir.setReturnValue(true);
        }
    }

    @ModifyArg(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer$Drawer;addRectangle(Lnet/minecraft/client/font/BakedGlyph$Rectangle;)V"))
    private BakedGlyph.Rectangle modifyDrawnRectangle(BakedGlyph.Rectangle rectangle) {
        ((HasMatrixProp) (Object) rectangle).setMatrix(this.currentMatrix);
        return rectangle;
    }

    @ModifyArg(method = "accept", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> E modifyDrawnGlyph(E e) {
        ((HasMatrixProp) e).setMatrix(this.currentMatrix);
        return e;
    }

    @ModifyArgs(
        method = "drawLayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/font/BakedGlyph;drawRectangle(Lnet/minecraft/client/font/BakedGlyph$Rectangle;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;I)V",
            ordinal = 1
        )
    )
    private void modifyDrawRectangleMatrix(Args args) {
        args.set(1, ((HasMatrixProp) args.get(0)).matrix());
    }

    @ModifyArgs(
        method = "drawGlyphs",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/font/BakedGlyph;draw(Lnet/minecraft/client/font/BakedGlyph$DrawnGlyph;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumer;I)V"
        )
    )
    private void modifyDrawMatrix(Args args) {
        args.set(1, ((HasMatrixProp) args.get(0)).matrix());
    }

}
