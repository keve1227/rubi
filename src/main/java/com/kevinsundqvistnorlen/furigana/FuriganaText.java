package com.kevinsundqvistnorlen.furigana;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.font.TextHandler;
import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jetbrains.annotations.*;
import org.joml.Math;
import org.joml.*;

import java.util.*;
import java.util.regex.Pattern;

public class FuriganaText implements OrderedText {

    public static final Pattern FURIGANA_PATTERN = Pattern.compile("\ue9c0?(\\p{L}+)\ue9c1([^\ue9c2]+)\ue9c2");
    public static final float FURIGANA_SCALE = 0.5f;
    public static final float TEXT_SCALE = 0.8f;

    private static final HashMap<UniqueOrderedText, FuriganaParseResult> CACHE = new HashMap<>();
    private static final int CACHE_MAX_SIZE = 1_000_000;

    private final @NotNull NonParseOrderedText text;
    private final @Nullable NonParseOrderedText furigana;

    public FuriganaText(@NotNull OrderedText text, @Nullable OrderedText furigana) {
        this.text = NonParseOrderedText.of(text);
        this.furigana = NonParseOrderedText.of(Utils.style(furigana, style -> style.withBold(true)));
    }

    public FuriganaText(@NotNull OrderedText text) {
        this(text, null);
    }

    private static OrderedText styledChars(CharSequence chars, Collection<Style> styles) {
        Queue<Style> queue = new ArrayDeque<>(styles);
        List<OrderedText> result = new ArrayList<>();
        chars.codePoints().forEachOrdered(codePoint -> result.add(OrderedText.styled(codePoint, queue.poll())));
        return OrderedText.innerConcat(result);
    }

    public static FuriganaParseResult parseCached(OrderedText text) {
        if (text.getClass().equals(FuriganaText.class)) {
            return new FuriganaParseResult((FuriganaText) text);
        }

        if (text.getClass().equals(NonParseOrderedText.class)) {
            return new FuriganaParseResult(new FuriganaText(text));
        }

        try {
            return CACHE.computeIfAbsent(new UniqueOrderedText(text), FuriganaText::parse);
        } finally {
            while (CACHE.size() >= CACHE_MAX_SIZE) {
                CACHE.remove(CACHE.keySet().stream().findAny().get());
            }
        }
    }

    private static FuriganaParseResult parse(OrderedText text) {
        if (text.getClass().equals(FuriganaText.class)) {
            return new FuriganaParseResult((FuriganaText) text);
        }

        if (text.getClass().equals(NonParseOrderedText.class)) {
            return new FuriganaParseResult(new FuriganaText(text));
        }

        List<FuriganaText> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        List<Style> styles = new ArrayList<>();

        text.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            styles.add(style);
            return true;
        });

        var matcher = FURIGANA_PATTERN.matcher(builder);
        int last = 0;

        while (matcher.find()) {
            var start = matcher.start();
            if (start > last) {
                var tail = FuriganaText.styledChars(builder.subSequence(last, start), styles.subList(last, start));
                result.add(new FuriganaText(tail, null));
            }

            var ruby = FuriganaText.styledChars(matcher.group(1), styles.subList(matcher.start(1), matcher.end(1)));
            var furigana = FuriganaText.styledChars(matcher.group(2), styles.subList(matcher.start(2), matcher.end(2)));
            result.add(new FuriganaText(ruby, furigana));

            last = matcher.end();
        }

        if (result.isEmpty()) {
            return new FuriganaParseResult(List.of(new FuriganaText(text)));
        }

        if (last < builder.length()) {
            var tail = FuriganaText.styledChars(
                builder.subSequence(last, builder.length()),
                styles.subList(last, builder.length())
            );
            result.add(new FuriganaText(tail));
        }

        return new FuriganaParseResult(result);
    }

    public NonParseOrderedText getText() {
        return this.text;
    }

    public NonParseOrderedText getFurigana() {
        return this.furigana == null ? NonParseOrderedText.of(Utils.orderedFrom("")) : this.furigana;
    }

    public boolean hasFurigana() {
        return this.furigana != null;
    }

    public float getWidth(TextHandler handler) {
        if (!this.hasFurigana()) {
            return handler.getWidth(this.getText());
        }

        return Math.max(
            handler.getWidth(this.getText()) * FuriganaText.TEXT_SCALE,
            handler.getWidth(this.getFurigana()) * FuriganaText.FURIGANA_SCALE
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
        float width = this.getWidth(handler);

        if (this.hasFurigana()) {
            MutableFloat xx = new MutableFloat();

            var furigana = this.getFurigana();
            float furiganaWidth = handler.getWidth(NonParseOrderedText.of(furigana)) * FuriganaText.FURIGANA_SCALE;
            float furiganaHeight = fontHeight * FuriganaText.FURIGANA_SCALE;
            float furiganaGap = (width - furiganaWidth) / Utils.charsFrom(furigana).length();

            xx.setValue(x + furiganaGap / 2);
            float yy = y - furiganaHeight;

            furigana.accept((index, style, codePoint) -> {
                var styled = NonParseOrderedText.of(OrderedText.styled(codePoint, style));

                drawer.draw(
                    styled,
                    xx.floatValue(),
                    yy,
                    new Matrix4f(matrix).scaleAround(
                        FuriganaText.FURIGANA_SCALE,
                        xx.floatValue(),
                        yy + furiganaHeight,
                        0
                    )
                );

                xx.add(handler.getWidth(styled) * FuriganaText.FURIGANA_SCALE + furiganaGap);
                return true;
            });

            var text = this.getText();
            float textWidth = handler.getWidth(text) * FuriganaText.TEXT_SCALE;
            float textHeight = fontHeight * FuriganaText.TEXT_SCALE;
            float textGap = (width - textWidth) / Utils.charsFrom(this.getText()).length();

            xx.setValue(x + textGap / 2);

            text.accept((index, style, codePoint) -> {
                var styled = NonParseOrderedText.of(OrderedText.styled(codePoint, style));

                drawer.draw(
                    styled,
                    xx.floatValue(),
                    y,
                    new Matrix4f(matrix).scaleAround(
                        FuriganaText.TEXT_SCALE,
                        xx.floatValue(),
                        y + textHeight,
                        0
                    )
                );

                xx.add(handler.getWidth(styled) * FuriganaText.TEXT_SCALE + textGap);
                return true;
            });
        } else {
            drawer.draw(this.text, x, y, matrix);
        }

        return width;
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return this.text.accept(visitor);
    }

    public record FuriganaParseResult(Collection<FuriganaText> texts) {

        public FuriganaParseResult(FuriganaText text) {
            this(ImmutableList.of(text));
        }

        public boolean hasFurigana() {
            return this.texts.stream().anyMatch(FuriganaText::hasFurigana);
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
                advance += text.draw(advance, y, matrix, handler, fontHeight, drawer);
            }

            return advance;
        }
    }

    public record NonParseOrderedText(OrderedText text) implements OrderedText {
        public NonParseOrderedText {
            if (text instanceof NonParseOrderedText) {
                throw new IllegalArgumentException("Cannot nest NonParseOrderedText");
            }
        }

        public static NonParseOrderedText of(OrderedText text) {
            if (text == null) return null;

            if (text.getClass() == NonParseOrderedText.class) {
                return (NonParseOrderedText) text;
            }

            return new NonParseOrderedText(text);
        }

        @Override
        public boolean accept(CharacterVisitor visitor) {
            return this.text.accept(visitor);
        }
    }
}
