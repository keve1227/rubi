package com.kevinsundqvistnorlen.furigana;

import net.minecraft.text.*;
import org.apache.commons.lang3.mutable.MutableLong;

record UniqueOrderedText(OrderedText text) implements OrderedText {
    private static final long FNV_OFFSET_BASIS = -3750763034362895579L;
    private static final long FNV_PRIME = 1099511628211L;

    public long longHashCode() {
        var hash = new MutableLong(UniqueOrderedText.FNV_OFFSET_BASIS);

        this.accept((index, style, codePoint) -> {
            long h = hash.longValue();
            h *= UniqueOrderedText.FNV_PRIME;
            h ^= codePoint;
            h *= UniqueOrderedText.FNV_PRIME;
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
        if (obj instanceof UniqueOrderedText other) {
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
