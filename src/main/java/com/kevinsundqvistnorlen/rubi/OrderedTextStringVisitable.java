package com.kevinsundqvistnorlen.rubi;

import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.Objects;
import java.util.Optional;

/**
 * A helper class that breaks down an {@link OrderedText} into sections with the same style.
 */
public final class OrderedTextStringVisitable implements StringVisitable {
    private final OrderedText text;

    public OrderedTextStringVisitable(OrderedText text) {
        this.text = text;
    }

    @Override
    public <T> Optional<T> visit(Visitor<T> visitor) {
        return this.visit((style, string) -> visitor.accept(string), Style.EMPTY);
    }

    @Override
    public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style parent) {
        if (Objects.equals(this.text, OrderedText.EMPTY)) {
            return Optional.empty();
        }

        Mutable<Optional<T>> result = new MutableObject<>(Optional.empty());
        StringBuilder pendingString = new StringBuilder();
        Mutable<Style> pendingStyle = new MutableObject<>();

        this.text.accept((index, style, codePoint) -> {
            Style parentedStyle = style.withParent(parent);
            if (!pendingString.isEmpty() && !pendingStyle.getValue().equals(parentedStyle)) {
                var optional = styledVisitor.accept(pendingStyle.getValue(), pendingString.toString());
                if (optional.isPresent()) {
                    result.setValue(optional);
                    return false;
                }

                pendingString.setLength(0);
            }

            pendingString.appendCodePoint(codePoint);
            pendingStyle.setValue(parentedStyle);
            return true;
        });

        if (result.getValue().isPresent()) {
            return result.getValue();
        }

        if (!pendingString.isEmpty()) {
            var optional = styledVisitor.accept(pendingStyle.getValue(), pendingString.toString());
            if (optional.isPresent()) {
                result.setValue(optional);
            }
        }

        return result.getValue();
    }
}
