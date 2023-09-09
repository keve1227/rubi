package com.kevinsundqvistnorlen.furigana;

import com.mojang.serialization.Codec;
import net.minecraft.client.option.*;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public enum FuriganaMode {
    ABOVE("above"),
    REPLACE("replace"),
    HIDDEN("hidden");

    private static final AtomicInteger VALUE = new AtomicInteger(0);

    public static final SimpleOption<FuriganaMode> OPTION = new SimpleOption<>(
        "options.furigana.mode",
        SimpleOption.emptyTooltip(),
        (optionText, value) -> value.getText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(
            Arrays.asList(FuriganaMode.values()),
            Codec.INT.xmap(FuriganaMode::byOrdinal, FuriganaMode::ordinal)
        ),
        FuriganaMode.ABOVE,
        (value) -> {
            Utils.LOGGER.info("Furigana mode set to {}", value);
            VALUE.set(value.ordinal());
        }
    );

    private final Text text;

    FuriganaMode(String name) {
        this.text = Text.translatable("options.furigana.mode." + name);
    }

    public static FuriganaMode getValue() {
        return byOrdinal(VALUE.get());
    }

    public static void accept(GameOptions.Visitor visitor) {
        visitor.accept("furigana.mode", OPTION);
        VALUE.set(OPTION.getValue().ordinal());
    }

    // public static void setValue(FuriganaMode value) {
    //     OPTION.setValue(value);
    // }

    public static FuriganaMode byOrdinal(int ordinal) {
        return FuriganaMode.values()[ordinal];
    }

    public Text getText() {
        return this.text;
    }
}
