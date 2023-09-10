package com.kevinsundqvistnorlen.rubi.option;

import com.kevinsundqvistnorlen.rubi.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.*;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public enum RubyMode {
    NORMAL("normal"),
    INVERSE("inverse"),
    REPLACE("replace"),
    HIDDEN("hidden");

    private static final AtomicInteger VALUE = new AtomicInteger(0);

    public static final SimpleOption<RubyMode> OPTION = new SimpleOption<>(
        "options.rubi.mode",
        SimpleOption.emptyTooltip(),
        (optionText, value) -> value.getText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(
            Arrays.asList(RubyMode.values()),
            Codec.INT.xmap(RubyMode::byOrdinal, RubyMode::ordinal)
        ),
        RubyMode.NORMAL,
        (value) -> {
            Utils.LOGGER.info("Ruby mode set to {}", value);
            VALUE.set(value.ordinal());
        }
    );

    private final Text text;

    RubyMode(String name) {
        this.text = Text.translatable("options.rubi.mode." + name);
    }

    public static RubyMode getValue() {
        return byOrdinal(VALUE.get());
    }

    public static void accept(GameOptions.Visitor visitor) {
        visitor.accept("rubi.mode", OPTION);
        VALUE.set(OPTION.getValue().ordinal());
    }

    // public static void setValue(RubyMode value) {
    //     OPTION.setValue(value);
    // }

    public static RubyMode byOrdinal(int ordinal) {
        return RubyMode.values()[ordinal];
    }

    public Text getText() {
        return this.text;
    }
}
