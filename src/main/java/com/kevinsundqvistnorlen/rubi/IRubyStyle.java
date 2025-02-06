package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.Style;

public interface IRubyStyle {
    static RubyText getRuby(Style style) {
        return ((IRubyStyle) style).getRuby();
    }

    Style withRuby(String word, String ruby);
    RubyText getRuby();
    Style removeRuby();
}
