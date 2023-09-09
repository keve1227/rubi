package com.kevinsundqvistnorlen.furigana;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.*;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.*;

import java.util.*;
import java.util.regex.Pattern;

public record FuriganaText(OrderedText text, OrderedText furigana) implements OrderedText {

    public static final Pattern FURIGANA_PATTERN = Pattern.compile("\ue9c0(\\p{L}+)\ue9c1([^\ue9c2]+)\ue9c2");

    public static final float FURIGANA_SCALE = 0.5f;
    public static final float FURIGANA_OVERLAP = 0.1f;
    public static final float TEXT_SCALE = 0.8f;

    private static final HashMap<OrderedTextKey, FuriganaParseResult> CACHE = new HashMap<>();
    private static final int CACHE_MAX_SIZE = 1_000_000;

    private static OrderedText styledChars(CharSequence chars, Collection<? extends Style> styles) {
        Queue<Style> queue = new ArrayDeque<>(styles);
        List<OrderedText> result = new ArrayList<>();
        chars.codePoints().forEachOrdered(codePoint -> result.add(OrderedText.styled(codePoint, queue.poll())));
        return OrderedText.innerConcat(result);
    }

    public static FuriganaParseResult cachedParse(OrderedText text) {
        if (text.getClass().equals(FuriganaText.class)) {
            return new FuriganaParseResult(text);
        }

        try {
            return Optional.ofNullable(CACHE.computeIfAbsent(new OrderedTextKey(text), FuriganaText::internalParse))
                .orElseGet(() -> new FuriganaParseResult(text));
        } finally {
            while (CACHE.size() >= CACHE_MAX_SIZE) {
                CACHE.remove(CACHE.keySet().stream().findAny().get());
            }
        }
    }

    private static @Nullable FuriganaParseResult internalParse(OrderedText text) {
        if (text.getClass().equals(FuriganaText.class)) {
            return new FuriganaParseResult(text);
        }

        StringBuilder builder = new StringBuilder();
        List<Style> styles = new ArrayList<>();

        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            styles.add(style);
            return true;
        });

        // A text with less than 4 characters can't possibly contain furigana.
        if (builder.length() < 4) return null; // null means a no-cache result.

        List<OrderedText> result = new ArrayList<>();

        var matcher = FURIGANA_PATTERN.matcher(builder);
        int last = 0;

        while (matcher.find()) {
            var start = matcher.start();
            if (start > last) {
                result.add(FuriganaText.styledChars(builder.subSequence(last, start), styles.subList(last, start)));
            }

            var ruby = FuriganaText.styledChars(matcher.group(1), styles.subList(matcher.start(1), matcher.end(1)));
            var furigana = FuriganaText.styledChars(matcher.group(2), styles.subList(matcher.start(2), matcher.end(2)));
            result.add(new FuriganaText(ruby, furigana));

            last = matcher.end();
        }

        if (result.isEmpty()) {
            return new FuriganaParseResult(text);
        }

        if (last < builder.length()) {
            result.add(FuriganaText.styledChars(
                builder.subSequence(last, builder.length()),
                styles.subList(last, builder.length())
            ));
        }

        return new FuriganaParseResult(result);
    }

    public float getWidth(TextHandler handler) {
        return Math.max(
            handler.getWidth(this.text()) * FuriganaText.TEXT_SCALE,
            handler.getWidth(this.furigana()) * FuriganaText.FURIGANA_SCALE
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
        return switch (FuriganaMode.getValue()) {
            case ABOVE -> this.drawAbove(x, y, matrix, handler, fontHeight, drawer);
            case REPLACE -> this.drawReplace(x, y, matrix, handler, drawer);
            case HIDDEN -> this.drawHidden(x, y, matrix, handler, drawer);
        };
    }

    public float drawAbove(
        float x,
        float y,
        Matrix4f matrix,
        TextHandler handler,
        int fontHeight,
        TextDrawer drawer
    ) {
        final float width = handler.getWidth(this);
        final float textHeight = fontHeight * FuriganaText.TEXT_SCALE;
        final float furiganaHeight = fontHeight * FuriganaText.FURIGANA_SCALE;

        final float yText = y + (fontHeight - textHeight);
        final float yFurigana = yText - furiganaHeight + fontHeight * FuriganaText.FURIGANA_OVERLAP;

        MutableFloat xx = new MutableFloat();

        var furigana = Utils.styleOrdered(this.furigana(), style -> style.withUnderline(false).withBold(true));
        float furiganaWidth = handler.getWidth(furigana) * FuriganaText.FURIGANA_SCALE;
        float furiganaGap = (width - furiganaWidth) / (Utils.charsFromOrdered(furigana).length() + 1);

        xx.setValue(x + furiganaGap);

        furigana.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);

            drawer.draw(
                styled,
                xx.floatValue(),
                yFurigana,
                new Matrix4f(matrix).scaleAround(
                    FuriganaText.FURIGANA_SCALE,
                    xx.floatValue(),
                    yFurigana,
                    0
                )
            );

            xx.add(handler.getWidth(styled) * FuriganaText.FURIGANA_SCALE + furiganaGap);
            return true;
        });

        var text = this.text();
        float textWidth = handler.getWidth(text) * FuriganaText.TEXT_SCALE;
        float textGap = (width - textWidth) / (Utils.charsFromOrdered(text).length() + 1);

        xx.setValue(x + textGap);

        text.accept((index, style, codePoint) -> {
            var styled = OrderedText.styled(codePoint, style);

            drawer.draw(
                styled,
                xx.floatValue(),
                yText,
                new Matrix4f(matrix).scaleAround(
                    FuriganaText.TEXT_SCALE,
                    xx.floatValue(),
                    yText,
                    0
                )
            );

            xx.add(handler.getWidth(styled) * FuriganaText.TEXT_SCALE + textGap);
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
        drawer.draw(this.furigana(), x, y, matrix);
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

    public record FuriganaParseResult(Collection<OrderedText> texts) {

        public FuriganaParseResult(OrderedText text) {
            this(ImmutableList.of(text));
        }

        public boolean hasFurigana() {
            for (final var text : this.texts) {
                if (text.getClass() == FuriganaText.class) return true;
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
                if (text.getClass() == FuriganaText.class) {
                    advance += ((FuriganaText) text).draw(advance, y, matrix, handler, fontHeight, drawer);
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
