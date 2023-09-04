package com.kevinsundqvistnorlen.furigana;

import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.text.*;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public final class Utils {

    // public static final Logger LOGGER = LoggerFactory.getLogger("Furigana");

    public static OrderedText orderedFrom(String text) {
        return Text.literal(text).asOrderedText();
    }

    public static OrderedText orderedFrom(StringVisitable text) {
        return visitor -> {
            return TextVisitFactory.visitFormatted(
                text,
                Style.EMPTY,
                (index, style, codePoint) -> {
                    return visitor.accept(index, style, codePoint);
                }
            );
        };
    }

    public static OrderedText orderedFrom(List<? extends OrderedText> texts) {
        return OrderedText.concat((List<OrderedText>) texts);
    }

    public static OrderedText style(OrderedText text, UnaryOperator<Style> stylizer) {
        if (text == null) return null;

        return visitor -> {
            return text.accept((index, style, codePoint) -> {
                return visitor.accept(index, stylizer.apply(style), codePoint);
            });
        };
    }
}
