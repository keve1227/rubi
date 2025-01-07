package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.HasMatrixProp;
import net.minecraft.client.font.BakedGlyph;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedGlyph.DrawnGlyph.class)
public class MixinDrawnGlyph implements HasMatrixProp {
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
