package com.kevinsundqvistnorlen.furigana;

import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;

@FunctionalInterface
public interface TextDrawer {
    void draw(OrderedText text, float x, float y, Matrix4f matrix);
}
