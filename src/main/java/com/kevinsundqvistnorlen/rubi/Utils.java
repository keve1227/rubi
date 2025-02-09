package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.UnaryOperator;

public final class Utils {
    public static final Logger LOGGER = LoggerFactory.getLogger("Rubi");

    public static OrderedText transformStyle(OrderedText text, UnaryOperator<Style> transformer) {
        return visitor -> text.accept(
            (index, style, codePoint) -> visitor.accept(index, transformer.apply(style), codePoint)
        );
    }
}
