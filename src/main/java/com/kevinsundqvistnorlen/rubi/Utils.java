package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger("Rubi");

    public static OrderedText orderedFrom(String text) {
        if (text.isEmpty()) {
            return OrderedText.EMPTY;
        }
        return visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
    }

    public static OrderedText orderedFrom(StringVisitable text) {
        return visitor -> TextVisitFactory.visitFormatted(text, Style.EMPTY, visitor);
    }

    public static int lengthOfOrdered(OrderedText text) {
        MutableInt length = new MutableInt();
        text.accept((index, style, codePoint) -> {
            length.increment();
            return true;
        });
        return length.getValue();
    }
}
