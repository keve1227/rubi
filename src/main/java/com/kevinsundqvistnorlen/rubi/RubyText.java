package com.kevinsundqvistnorlen.rubi;

import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.regex.Pattern;

public record RubyText(String text, String ruby, Style style) {
    public static final Pattern RUBY_PATTERN = Pattern.compile("\\^\\s*(.+?)\\s*\\(\\s*(.+?)\\s*\\)");
    public static final Pattern RUBY_PATTERN_FOR_STRIPPING = Pattern.compile("§" + RUBY_PATTERN.pattern());

    private static final float RUBY_SCALE = 0.5f;
    private static final float RUBY_OVERLAP = 0.1f;
    private static final float TEXT_SCALE = 0.8f;

    public static String strip(String returnValue) {
        StringBuilder sb = new StringBuilder(returnValue.length());
        var matcher = RUBY_PATTERN_FOR_STRIPPING.matcher(returnValue);
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    float draw(
        float x, float y, Matrix4f matrix, TextHandler textHandler, int fontHeight,
        TextDrawer textDrawer
    ) {
        float width = this.getWidth(textHandler);

        switch (RubyRenderMode.getOption().getValue()) {
            case ABOVE -> this.drawAbove(x, y, width, matrix, textHandler, fontHeight, textDrawer);
            case BELOW -> this.drawBelow(x, y, width, matrix, textHandler, fontHeight, textDrawer);
            case REPLACE -> this.drawReplace(x, y, matrix, textDrawer);
            case HIDDEN -> this.drawHidden(x, y, matrix, textDrawer);
        }

        return width;
    }

    public float getWidth(TextHandler textHandler) {
        final var mode = RubyRenderMode.getOption().getValue();
        float baseWidth = 0f, rubyWidth = 0f;

        if (mode != RubyRenderMode.REPLACE) {
            baseWidth += textHandler.getWidth(StringVisitable.styled(this.text(), this.style()));
        }

        if (mode != RubyRenderMode.HIDDEN) {
            rubyWidth += textHandler.getWidth(
                StringVisitable.styled(this.ruby(), this.style().withUnderline(false).withStrikethrough(false)));
        }

        return switch (mode) {
            case ABOVE, BELOW -> Math.max(baseWidth * RubyText.TEXT_SCALE, rubyWidth * RubyText.RUBY_SCALE);
            case HIDDEN -> baseWidth;
            case REPLACE -> rubyWidth;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) return false;
        if (this == o) return true;
        RubyText other = (RubyText) o;
        return Objects.equals(this.text(), other.text()) && Objects.equals(this.ruby(), other.ruby()) && Objects.equals(
            this.style(), other.style());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.text(), this.ruby(), this.style());
    }

    private void drawRubyPair(
        float x, float yText, float yRuby, float width, TextDrawer textDrawer,
        TextHandler textHandler, Matrix4f matrix
    ) {
        textDrawer.drawSpacedApart(
            OrderedText.styledForwardsVisitedString(this.text(), this.style()), x, yText, RubyText.TEXT_SCALE, width,
            matrix, textHandler
        );

        textDrawer.drawSpacedApart(
            OrderedText.styledForwardsVisitedString(
                this.ruby(),
                this.style().withUnderline(false).withStrikethrough(false)
            ), x, yRuby, RubyText.RUBY_SCALE, width, matrix, textHandler
        );
    }

    private void drawAbove(
        float x, float y, float width, Matrix4f matrix, TextHandler textHandler, int fontHeight,
        TextDrawer textDrawer
    ) {
        float textHeight = fontHeight * RubyText.TEXT_SCALE;
        float rubyHeight = fontHeight * RubyText.RUBY_SCALE;

        float yBody = y + (fontHeight - textHeight);
        float yAbove = yBody - rubyHeight + fontHeight * RubyText.RUBY_OVERLAP;

        this.drawRubyPair(x, yBody, yAbove, width, textDrawer, textHandler, matrix);
    }

    private void drawBelow(
        float x, float y, float width, Matrix4f matrix, TextHandler textHandler, int fontHeight,
        TextDrawer textDrawer
    ) {
        float textHeight = fontHeight * RubyText.TEXT_SCALE;
        float yBelow = y + textHeight - fontHeight * RubyText.RUBY_OVERLAP;

        this.drawRubyPair(x, y, yBelow, width, textDrawer, textHandler, matrix);
    }

    private void drawReplace(float x, float y, Matrix4f matrix, TextDrawer textDrawer) {
        textDrawer.draw(OrderedText.styledForwardsVisitedString(this.ruby(), this.style), x, y, matrix);
    }

    private void drawHidden(float x, float y, Matrix4f matrix, TextDrawer textDrawer) {
        textDrawer.draw(OrderedText.styledForwardsVisitedString(this.text(), this.style), x, y, matrix);
    }
}
