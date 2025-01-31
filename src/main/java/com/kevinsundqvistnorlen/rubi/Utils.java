package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.*;

import java.util.function.Consumer;

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

    public static void splitWithDelimitersOrderedText(
        OrderedText text,
        int splitCodePoint,
        Consumer<OrderedText> splitConsumer,
        Consumer<OrderedText> delimiterConsumer
    ) {
        MutableObject<OrderedText> current = new MutableObject<>(OrderedText.EMPTY);
        text.accept((index, style, codePoint) -> {
            if (codePoint == splitCodePoint) {
                splitConsumer.accept(current.getValue());
                current.setValue(OrderedText.EMPTY);
                delimiterConsumer.accept(OrderedText.styled(codePoint, style));
                return true;
            }
            current.setValue(OrderedText.innerConcat(current.getValue(), OrderedText.styled(codePoint, style)));
            return true;
        });
        splitConsumer.accept(current.getValue());
    }
}
