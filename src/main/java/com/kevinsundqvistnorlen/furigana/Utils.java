package com.kevinsundqvistnorlen.furigana;

import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.joml.Math;
import org.joml.*;

import java.util.List;
import java.util.function.UnaryOperator;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public final class Utils {

    // public static final Logger LOGGER = LoggerFactory.getLogger("Furigana");

    public static OrderedText orderedFrom(String text) {
        return Text.literal(text).asOrderedText();
    }

    public static OrderedText orderedFrom(StringVisitable text) {
        return visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
    }

    public static OrderedText orderedFrom(List<? extends OrderedText> texts) {
        return OrderedText.concat((List<OrderedText>) texts);
    }

    public static OrderedText style(OrderedText text, UnaryOperator<Style> stylizer) {
        if (text == null) return null;

        return visitor -> text.accept((index, style, codePoint) -> {
            return visitor.accept(index, stylizer.apply(style), codePoint);
        });
    }

    public static int drawFuriganaTexts(
        List<FuriganaText> texts,
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        Drawer drawer
    ) {
        float advance = x;

        for (final var text : texts) {
            float textWidth = handler.getWidth(text);
            var furigana = Utils.style(text.getFurigana(), style -> style.withBold(true));

            if (furigana != null) {
                final float heightScale = 0.5f;

                float furiganaWidth = handler.getWidth(furigana);
                float scale = Math.min(heightScale, textWidth / furiganaWidth);

                float lineHeight = fontHeight * heightScale;
                float xx = advance + (textWidth - furiganaWidth * scale) / 2;
                float yy = y - lineHeight;

                drawer.draw(
                    furigana,
                    xx,
                    yy,
                    new Matrix4f(matrix).scaleAround(scale, heightScale, 1, xx, yy + lineHeight, 0)
                );
                drawer.draw(
                    text,
                    advance,
                    y,
                    new Matrix4f(matrix).scaleAround(1, 0.8f, 1, advance + textWidth / 2, y + fontHeight * 0.8f, 0)
                );
            } else {
                drawer.draw(text, advance, y, matrix);
            }

            advance += textWidth;
        }

        return (int) Math.floor(advance);
    }

    @FunctionalInterface
    public interface Drawer {
        void draw(OrderedText text, float x, float y, Matrix4f matrix);
    }
}
