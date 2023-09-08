package com.kevinsundqvistnorlen.furigana;

import net.minecraft.text.*;

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

    public static CharSequence charsFrom(OrderedText text) {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });

        return builder;
    }
}
