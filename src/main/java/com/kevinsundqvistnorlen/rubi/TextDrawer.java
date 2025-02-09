package com.kevinsundqvistnorlen.rubi;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Matrix4f;

import java.util.Optional;

@FunctionalInterface
public interface TextDrawer {
    static float draw(
        OrderedText text, float x, float y, Matrix4f matrix, TextHandler handler, int fontHeight, TextDrawer drawer) {
        var xx = new MutableFloat(x);
        new OrderedTextStringVisitable(text).visit(
            (style, string) -> {
                IRubyStyle.getRuby(style).ifPresentOrElse(
                    ruby -> xx.add(ruby.draw(xx.getValue(), y, matrix, handler, fontHeight, drawer)), () -> {
                        var orderedText = OrderedText.styledForwardsVisitedString(string, style);
                        drawer.draw(orderedText, xx.getAndAdd(handler.getWidth(orderedText)), y, matrix);
                    }
                );
                return Optional.empty();
            }, Style.EMPTY
        );
        return xx.getValue();
    }

    void draw(OrderedText text, float x, float y, Matrix4f matrix);

    default void drawScaled(OrderedText text, float x, float y, float scale, Matrix4f matrix) {
        this.draw(text, x, y, new Matrix4f(matrix).scaleAround(scale, x, y, 0));
    }

    default void drawSpacedApart(
        OrderedText text, float x, float y, float scale, float boxWidth, Matrix4f matrix, TextHandler handler) {
        float width = handler.getWidth(text) * scale;
        float gap = (boxWidth - width) / Utils.lengthOfOrdered(text);

        var xx = new MutableFloat(x + gap / 2);
        text.accept((index, style, codePoint) -> {
            var styledChar = OrderedText.styled(codePoint, style);
            this.drawScaled(styledChar, xx.floatValue(), y, scale, matrix);
            xx.add(handler.getWidth(styledChar) * scale + gap);
            return true;
        });
    }
}
