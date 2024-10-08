package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.*;

import java.util.*;
import java.util.function.UnaryOperator;

public final class Utils {

    public static final Logger LOGGER = LoggerFactory.getLogger("Rubi");

    public static OrderedText orderedFrom(String text) {
        return Text.literal(text).asOrderedText();
    }

    public static OrderedText orderedFrom(StringVisitable text) {
        return visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
    }

    public static OrderedText orderedFrom(CharSequence chars, Collection<? extends Style> styles) {
        Queue<Style> queue = new ArrayDeque<>(styles);
        List<OrderedText> result = new ArrayList<>();
        chars.codePoints().forEachOrdered(codePoint -> result.add(OrderedText.styled(codePoint, queue.poll())));
        return OrderedText.innerConcat(result);
    }

    public static OrderedText styleOrdered(@NotNull OrderedText text, UnaryOperator<Style> stylizer) {
        return visitor -> text.accept((index, style, codePoint) -> {
            return visitor.accept(index, stylizer.apply(style), codePoint);
        });
    }

    public static CharSequence charsFromOrdered(OrderedText text) {
        StringBuilder builder = new StringBuilder();
        text.accept((index, style, codePoint) -> !builder.appendCodePoint(codePoint).isEmpty());
        return builder;
    }
}
