package com.kevinsundqvistnorlen.furigana;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import net.minecraft.text.CharacterVisitor;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;

public class FuriganaText implements OrderedText {

    private static final Pattern FURIGANA_PATTERN = Pattern.compile("\\s?(\\p{L}+)\\{([^}]+)}");
    private static final HashMap<UniqueOrderedText, List<FuriganaText>> CACHE = new HashMap<>();

    private final OrderedText text;
    private final OrderedText furigana;

    public FuriganaText(OrderedText text, OrderedText furigana) {
        this.text = text;
        this.furigana = furigana;
    }

    public OrderedText getText() {
        return text;
    }

    public OrderedText getFurigana() {
        return furigana;
    }

    @Override
    public boolean accept(CharacterVisitor visitor) {
        return text.accept(visitor);
    }

    private static OrderedText styledChars(CharSequence chars, Collection<Style> styles) {
        Queue<Style> queue = new ArrayDeque<>(styles);
        List<OrderedText> result = new ArrayList<>();
        chars.codePoints().forEachOrdered(codePoint -> result.add(OrderedText.styled(codePoint, queue.poll())));
        return OrderedText.innerConcat(result);
    }

    public static List<FuriganaText> parseCached(OrderedText text) {
        try {
            return CACHE.computeIfAbsent(new UniqueOrderedText(text), FuriganaText::parse);
        } finally {
            while (CACHE.size() >= 1_000_000) {
                CACHE.remove(CACHE.keySet().stream().findAny().get());
            }
        }
    }

    public static List<FuriganaText> parse(OrderedText text) {
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
            return List.of(new FuriganaText(text, null));
        }

        if (last < builder.length()) {
            var tail = FuriganaText.styledChars(builder.subSequence(last, builder.length()), styles.subList(last, builder.length()));
            result.add(new FuriganaText(tail, null));
        }

        return result;
    }

    public static OrderedText strip(OrderedText text) {
        return Utils.orderedFrom(FuriganaText.parseCached(text));
    }

    private record UniqueOrderedText(OrderedText text) implements OrderedText {
        @Override
        public boolean accept(CharacterVisitor visitor) {
            return text.accept(visitor);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof UniqueOrderedText) {
                var other = (UniqueOrderedText) obj;
                return other.longHashCode() == this.longHashCode();
            }

            return false;
        }

        public long longHashCode() {
            var hash = new AtomicLong(-3750763034362895579L);

            this.accept((index, style, codePoint) -> {
                    long h = hash.getPlain();
                    h *= 1099511628211L;
                    h ^= codePoint;
                    h *= 1099511628211L;
                    h ^= style.hashCode();
                    hash.setPlain(h);
                    return true;
                });

            return hash.getPlain();
        }

        @Override
        public int hashCode() {
            long hash = this.longHashCode();
            return (int) (hash ^ (hash >>> 32));
        }
    }
}
