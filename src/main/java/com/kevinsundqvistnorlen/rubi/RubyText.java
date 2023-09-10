package com.kevinsundqvistnorlen.rubi;

import com.google.common.collect.ImmutableList;
import com.kevinsundqvistnorlen.rubi.option.RubyMode;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.*;

import java.util.*;
import java.util.regex.Pattern;

public record RubyText(OrderedText text, OrderedText ruby) implements OrderedText {

    public static final Pattern RUBY_PATTERN = Pattern.compile("\ue9c0([^\ue9c1]+)\ue9c1([^\ue9c2]+)\ue9c2");

    public static final float RUBY_SCALE = 0.5f;
    public static final float RUBY_OVERLAP = 0.1f;
    public static final float TEXT_SCALE = 0.8f;

    private static final HashMap<OrderedTextKey, RubyParseResult> CACHE = new HashMap<>();
    private static final int CACHE_MAX_SIZE = 1_000_000;

    private static OrderedText styledChars(CharSequence chars, Collection<? extends Style> styles) {
        Queue<Style> queue = new ArrayDeque<>(styles);
        List<OrderedText> result = new ArrayList<>();
        chars.codePoints().forEachOrdered(codePoint -> result.add(OrderedText.styled(codePoint, queue.poll())));
        return OrderedText.innerConcat(result);
    }

    public static RubyParseResult cachedParse(OrderedText text) {
        if (text.getClass().equals(RubyText.class)) {
            return new RubyParseResult(text);
        }

        try {
            return Optional.ofNullable(CACHE.computeIfAbsent(new OrderedTextKey(text), RubyText::internalParse))
                .orElseGet(() -> new RubyParseResult(text));
        } finally {
            while (CACHE.size() >= CACHE_MAX_SIZE) {
                CACHE.remove(CACHE.keySet().stream().findAny().get());
            }
        }
    }

    private static @Nullable RubyText.RubyParseResult internalParse(OrderedText text) {
        if (text.getClass().equals(RubyText.class)) {
            return new RubyParseResult(text);
        }

        StringBuilder builder = new StringBuilder();
        List<Style> styles = new ArrayList<>();

        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            styles.add(style);
            return true;
        });

        // A text with less than 4 characters can't possibly contain ruby.
        if (builder.length() < 4) return null; // null means a no-cache result.

        List<OrderedText> result = new ArrayList<>();

        var matcher = RUBY_PATTERN.matcher(builder);
        int last = 0;

        while (matcher.find()) {
            var start = matcher.start();
            if (start > last) {
                result.add(RubyText.styledChars(builder.subSequence(last, start), styles.subList(last, start)));
            }

            var body = RubyText.styledChars(matcher.group(1), styles.subList(matcher.start(1), matcher.end(1)));
            var ruby = RubyText.styledChars(matcher.group(2), styles.subList(matcher.start(2), matcher.end(2)));
            result.add(new RubyText(body, ruby));

            last = matcher.end();
        }

        if (result.isEmpty()) {
            return new RubyParseResult(text);
        }

        if (last < builder.length()) {
            result.add(RubyText.styledChars(
                builder.subSequence(last, builder.length()),
                styles.subList(last, builder.length())
            ));
        }

        return new RubyParseResult(result);
    }

    public float getWidth(TextHandler handler) {
        return Math.max(
            handler.getWidth(this.text()) * RubyText.TEXT_SCALE,
            handler.getWidth(this.ruby()) * RubyText.RUBY_SCALE
        );
    }

    public float draw(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        return switch (RubyMode.getValue()) {
            case NORMAL, INVERSE -> this.drawNormal(x, y, matrix, handler, fontHeight, drawer);
            case REPLACE -> this.drawReplace(x, y, matrix, handler, drawer);
            case HIDDEN -> this.drawHidden(x, y, matrix, handler, drawer);
        };
    }

    public float drawNormal(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        final RubyMode mode = RubyMode.getValue();

        final float width = handler.getWidth(this);
        final float textHeight = fontHeight * RubyText.TEXT_SCALE;
        final float overHeight = fontHeight * RubyText.RUBY_SCALE;

        final float yMain = y + (fontHeight - textHeight);
        final float yOver = yMain - overHeight + fontHeight * RubyText.RUBY_OVERLAP;

        MutableFloat xx = new MutableFloat();

        var over = Utils.styleOrdered(
            mode != RubyMode.INVERSE ? this.ruby() : this.text(),
            style -> style.withUnderline(false).withBold(true)
        );

        float overWidth = handler.getWidth(over) * RubyText.RUBY_SCALE;
        float overGap = (width - overWidth) / Utils.charsFromOrdered(over).length();
        xx.setValue(x + overGap / 2);

        over.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);

            drawer.draw(
                styled,
                xx.floatValue(),
                yOver,
                new Matrix4f(matrix).scaleAround(
                    RubyText.RUBY_SCALE,
                    xx.floatValue(),
                    yOver,
                    0
                )
            );

            xx.add(handler.getWidth(styled) * RubyText.RUBY_SCALE + overGap);
            return true;
        });

        var main = mode != RubyMode.INVERSE ? this.text() : this.ruby();

        float mainWidth = handler.getWidth(main) * RubyText.TEXT_SCALE;
        float mainGap = (width - mainWidth) / Utils.charsFromOrdered(main).length();
        xx.setValue(x + mainGap / 2);

        main.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);

            drawer.draw(
                styled,
                xx.floatValue(),
                yMain,
                new Matrix4f(matrix).scaleAround(
                    RubyText.TEXT_SCALE,
                    xx.floatValue(),
                    yMain,
                    0
                )
            );

            xx.add(handler.getWidth(styled) * RubyText.TEXT_SCALE + mainGap);
            return true;
        });

        return width;
    }

    public float drawReplace(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        TextDrawer drawer
    ) {
        drawer.draw(this.ruby(), x, y, matrix);
        return handler.getWidth(this);
    }

    public float drawHidden(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        TextDrawer drawer
    ) {
        drawer.draw(this.text(), x, y, matrix);
        return handler.getWidth(this);
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return OrderedText.concat(
            OrderedText.styled('\ue9c0', Style.EMPTY),
            this.text,
            OrderedText.styled('\ue9c1', Style.EMPTY),

            OrderedText.styled('\ue9c2', Style.EMPTY)
        ).accept(visitor);
    }

    public record RubyParseResult(Collection<OrderedText> texts) {

        public RubyParseResult(OrderedText text) {
            this(ImmutableList.of(text));
        }

        public boolean hasRuby() {
            for (final var text : this.texts) {
                if (text.getClass() == RubyText.class) return true;
            }

            return false;
        }

        public float draw(
            float x,
            float y,
            Matrix4f matrix,
            TextHandler handler,
            int fontHeight,
            TextDrawer drawer
        ) {
            float advance = x;

            for (final var text : this.texts) {
                if (text.getClass() == RubyText.class) {
                    advance += ((RubyText) text).draw(advance, y, matrix, handler, fontHeight, drawer);
                } else {
                    drawer.draw(text, advance, y, matrix);
                    advance += handler.getWidth(text);
                }
            }

            return advance;
        }
    }

    private record OrderedTextKey(OrderedText text) implements OrderedText {
        private static final long FNV_OFFSET_BASIS = -3750763034362895579L;
        private static final long FNV_PRIME = 1099511628211L;

        public long longHashCode() {
            var hash = new MutableLong(OrderedTextKey.FNV_OFFSET_BASIS);

            this.accept((index, style, codePoint) -> {
                long h = hash.longValue();
                h *= OrderedTextKey.FNV_PRIME;
                h ^= codePoint;
                h *= OrderedTextKey.FNV_PRIME;
                h ^= style.hashCode();
                hash.setValue(h);
                return true;
            });

            return hash.longValue();
        }

        @Override
        public boolean accept(CharacterVisitor visitor) {
            return this.text.accept(visitor);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof OrderedTextKey other) {
                return other.longHashCode() == this.longHashCode();
            }

            return false;
        }

        @Override
        public int hashCode() {
            long hash = this.longHashCode();
            return (int) (hash ^ (hash >>> 32));
        }
    }
}
