package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.OrderedText;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger("Rubi");

    public static int lengthOfOrdered(OrderedText text) {
        MutableInt length = new MutableInt();
        text.accept((index, style, codePoint) -> {
            length.increment();
            return true;
        });
        return length.getValue();
    }
}
