package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.Style;

public interface IRubyStyle {
    Style withRuby(String word, String ruby);
    RubyText getRuby();
    Style removeRuby();
}
