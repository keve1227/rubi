package com.kevinsundqvistnorlen.rubi;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Matrix4f;

@FunctionalInterface
public interface TextDrawer {
    static float draw(
        OrderedText text, float x, float y, Matrix4f matrix, TextHandler textHandler, int fontHeight,
        TextDrawer textDrawer
    ) {
        var xx = new MutableFloat(x);
        text.accept((index, style, codePoint) -> {
            xx.add(IRubyStyle
                .getRuby(style)
                .map(rubyText -> rubyText.draw(xx.getValue(), y, matrix, textHandler, fontHeight, textDrawer))
                .orElseGet(() -> {
                    var styledChar = OrderedText.styled(codePoint, style);
                    textDrawer.draw(styledChar, xx.floatValue(), y, matrix);
                    return textHandler.getWidth(styledChar);
                }));
            return true;
        });
        return xx.getValue();
    }

    void draw(OrderedText text, float x, float y, Matrix4f matrix);

    default void drawScaled(OrderedText text, float x, float y, float scale, Matrix4f matrix) {
        this.draw(text, x, y, new Matrix4f(matrix).scaleAround(scale, x, y, 0));
    }

    default void drawSpacedApart(
        OrderedText text, float x, float y, float scale, float boxWidth, Matrix4f matrix, TextHandler handler) {
        float textWidth = handler.getWidth(text) * scale;
        float emptySpace = boxWidth - textWidth;

        var xx = new MutableFloat(x);
        text.accept((index, style, codePoint) -> {
            var styledChar = OrderedText.styled(codePoint, style);
            float charWidth = handler.getWidth(styledChar) * scale;
            float spaceAround = emptySpace * (charWidth / textWidth);
            xx.add(spaceAround / 2);
            this.drawScaled(styledChar, xx.floatValue(), y, scale, matrix);
            xx.add(charWidth + spaceAround / 2);
            return true;
        });
    }
}
