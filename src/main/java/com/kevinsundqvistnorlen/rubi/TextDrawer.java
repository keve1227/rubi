package com.kevinsundqvistnorlen.rubi;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.*;

import java.lang.Math;

@FunctionalInterface
public interface TextDrawer {
    void draw(OrderedText text, float x, float y, Matrix4f matrix);

    default void drawScaled(
        OrderedText text,
        float x,
        float y,
        float scale,
        Matrix4f matrix
    ) {
        this.draw(text, x, y, new Matrix4f(matrix).scaleAround(scale, x, y, 0));
    }

    default void drawSpacedApart(
        OrderedText text,
        float x,
        float y,
        float scale,
        float boxWidth,
        Matrix4f matrix,
        TextHandler handler
    ) {
        float width = handler.getWidth(text) * scale;
        float gap = (boxWidth - width) / Utils.charsFromOrdered(text).length();

        var xx = new MutableFloat(x + gap / 2);
        text.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);
            this.drawScaled(styled, xx.floatValue(), y, scale, matrix);
            xx.add(handler.getWidth(styled) * scale + gap);
            return true;
        });
    }

    static int draw(
        OrderedText text,
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        final var advance = new MutableFloat(x);
        text.accept((index, style, codePoint) -> {
            if (codePoint == RubyText.RUBY_MARKER) {
                advance.add((int) Math.ceil(((IRubyStyle) style).getRuby().draw(
                    advance.getValue(),
                    y,
                    matrix,
                    style,
                    handler,
                    fontHeight,
                    drawer
                )));
                return true;
            }
            // TODO: Group together into one OrderedText
            var character = OrderedText.styled(codePoint, style);
            drawer.draw(character, advance.getAndAdd((int) handler.getWidth(character)), y, matrix);
            return true;
        });

        return advance.getValue().intValue();
    }

    static int draw(
        String text,
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        return 0;
    }
}
