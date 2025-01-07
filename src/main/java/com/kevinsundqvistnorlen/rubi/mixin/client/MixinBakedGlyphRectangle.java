package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.HasMatrixProp;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net/minecraft/client/font/BakedGlyph$Rectangle")
public class MixinBakedGlyphRectangle implements HasMatrixProp {
    @Unique
    private Matrix4f matrix;

    @Override
    public Matrix4f matrix() {
        return this.matrix;
    }

    @Override
    public void setMatrix(Matrix4f m) {
        this.matrix = m;
    }
}
