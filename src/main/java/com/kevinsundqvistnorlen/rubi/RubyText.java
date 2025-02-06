package com.kevinsundqvistnorlen.rubi;

import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.regex.Pattern;

public record RubyText(String text, String ruby, Style style) {
    public static final Pattern RUBY_PATTERN = Pattern.compile("\\^\\s*(.+?)\\s*\\(\\s*(.+?)\\s*\\)");
    public static final Pattern RUBY_PATTERN_FOR_STRIPPING = Pattern.compile("§" + RUBY_PATTERN.pattern());

    public static final float RUBY_SCALE = 0.5f;
    public static final float RUBY_OVERLAP = 0.1f;
    public static final float TEXT_SCALE = 0.8f;

    public static String strip(String returnValue) {
        StringBuilder sb = new StringBuilder(returnValue.length());
        var matcher = RUBY_PATTERN_FOR_STRIPPING.matcher(returnValue);
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public float draw(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        float width = handler.getWidth(OrderedText.styled('￼', this.style));

        switch (RubyRenderMode.getOption().getValue()) {
            case ABOVE -> this.drawAbove(x, y, width, matrix, handler, fontHeight, drawer);
            case BELOW -> this.drawBelow(x, y, width, matrix, handler, fontHeight, drawer);
            case REPLACE -> this.drawReplace(x, y, matrix, drawer);
            case HIDDEN -> this.drawHidden(x, y, matrix, drawer);
        }

        return width;
    }

    public float getWidth(TextHandler.WidthRetriever widthRetriever) {
        final var mode = RubyRenderMode.getOption().getValue();
        MutableFloat baseWidth = new MutableFloat(), rubyWidth = new MutableFloat();

        if (mode != RubyRenderMode.REPLACE) {
            TextVisitFactory.visitForwards(this.text(), this.style, (unused, s, codePoint) -> {
                baseWidth.add(widthRetriever.getWidth(codePoint, s));
                return true;
            });
        }

        if (mode != RubyRenderMode.HIDDEN) {
            TextVisitFactory.visitForwards(this.ruby(), this.style, (unused, s, codePoint) -> {
                rubyWidth.add(widthRetriever.getWidth(codePoint, s));
                return true;
            });
        }

        return switch (mode) {
            case ABOVE, BELOW ->
                Math.max(baseWidth.getValue() * RubyText.TEXT_SCALE, rubyWidth.getValue() * RubyText.RUBY_SCALE);
            case HIDDEN -> baseWidth.getValue();
            case REPLACE -> rubyWidth.getValue();
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        RubyText other = (RubyText) o;
        return Objects.equals(this.text, other.text()) && Objects.equals(this.ruby, other.ruby());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.text, this.ruby);
    }

    private void drawRubyPair(
        float x,
        float yText,
        float yRuby,
        float width,
        TextDrawer drawer,
        TextHandler handler,
        Matrix4f matrix
    ) {
        final var style = ((IRubyStyle) this.style).rubi$removeRuby();

        drawer.drawSpacedApart(
            OrderedText.styledForwardsVisitedString(this.text, style),
            x,
            yText,
            RubyText.TEXT_SCALE,
            width,
            matrix,
            handler
        );

        drawer.drawSpacedApart(
            OrderedText.styledForwardsVisitedString(this.ruby, style.withUnderline(false).withStrikethrough(false)),
            x,
            yRuby,
            RubyText.RUBY_SCALE,
            width,
            matrix,
            handler
        );
    }

    public void drawAbove(
        float x,
        float y,
        float width,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        float textHeight = fontHeight * RubyText.TEXT_SCALE;
        float rubyHeight = fontHeight * RubyText.RUBY_SCALE;

        float yBody = y + (fontHeight - textHeight);
        float yAbove = yBody - rubyHeight + fontHeight * RubyText.RUBY_OVERLAP;

        this.drawRubyPair(x, yBody, yAbove, width, drawer, handler, matrix);
    }

    public void drawBelow(
        float x,
        float y,
        float width,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        float textHeight = fontHeight * RubyText.TEXT_SCALE;
        float yBelow = y + textHeight - fontHeight * RubyText.RUBY_OVERLAP;

        this.drawRubyPair(x, y, yBelow, width, drawer, handler, matrix);
    }

    public void drawReplace(float x, float y, Matrix4f matrix, TextDrawer drawer) {
        drawer.draw(OrderedText.styledForwardsVisitedString(this.ruby, this.style), x, y, matrix);
    }

    public void drawHidden(float x, float y, Matrix4f matrix, TextDrawer drawer) {
        drawer.draw(OrderedText.styledForwardsVisitedString(this.text, this.style), x, y, matrix);
    }
}
