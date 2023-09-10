package com.kevinsundqvistnorlen.rubi;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.*;

@FunctionalInterface
public interface TextDrawer {
    void draw(OrderedText text, float x, float y, Matrix4f matrix);

    default void drawSpacedApart(
        OrderedText text,
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        float totalWidth
    ) {
        float scaleX = new Vector4f(1, 0, 0, 0).mul(matrix).x;
        float width = handler.getWidth(text) * scaleX;
        float gap = (totalWidth - width) / Utils.charsFromOrdered(text).length();

        var offsetX = new MutableFloat(gap / 2);
        text.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);
            this.draw(styled, x + offsetX.floatValue() / scaleX, y, matrix);
            offsetX.add(handler.getWidth(styled) * scaleX + gap);
            return true;
        });
    }
}
