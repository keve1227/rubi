package com.kevinsundqvistnorlen.rubi.option;

import net.minecraft.text.OrderedText;

@FunctionalInterface
public interface ITextHandler {
    float getWidth(OrderedText text);
}
