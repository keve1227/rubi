package com.kevinsundqvistnorlen.rubi;

import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.util.*;
import java.util.regex.Pattern;

public record RubyText(String text, String ruby) {

    public static final Pattern RUBY_PATTERN = Pattern.compile("\\^\\s*(.+?)\\s*\\(\\s*(.+?)\\s*\\)");

    public static final char RUBY_MARKER = '\ue9c0';

    public static final float RUBY_SCALE = 0.5f;
    public static final float RUBY_OVERLAP = 0.1f;
    public static final float TEXT_SCALE = 0.8f;

    public float getWidth(TextHandler.WidthRetriever widthRetriever, Style style) {
        var mode = RubyRenderMode.getOption().getValue();
        MutableFloat baseWidth = new MutableFloat(),
                     rubyWidth = new MutableFloat();
        if (mode != RubyRenderMode.REPLACE) TextVisitFactory.visitForwards(this.text(), style, (unused, s, codePoint) -> {
            baseWidth.add(widthRetriever.getWidth(codePoint, s));
            return true;
        });
        if (mode != RubyRenderMode.HIDDEN) TextVisitFactory.visitForwards(this.ruby(), style, (unused, s, codePoint) -> {
            rubyWidth.add(widthRetriever.getWidth(codePoint, s));
            return true;
        });
        return switch (mode) {
            case ABOVE, BELOW -> Math.max(
                baseWidth.getValue() * RubyText.TEXT_SCALE,
                rubyWidth.getValue() * RubyText.RUBY_SCALE
            );
            case HIDDEN -> baseWidth.getValue();
            case REPLACE -> rubyWidth.getValue();
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RubyText rubyText = (RubyText) o;
        return Objects.equals(text, rubyText.text) && Objects.equals(ruby, rubyText.ruby);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, ruby);
    }
}
