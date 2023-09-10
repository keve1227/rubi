package com.kevinsundqvistnorlen.rubi.option;

import com.kevinsundqvistnorlen.rubi.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.*;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;

public enum RubyDisplayMode {
    ABOVE("options.rubi.rubyDisplayMode.above"),
    BELOW("options.rubi.rubyDisplayMode.below"),
    REPLACE("options.rubi.rubyDisplayMode.replace"),
    OFF("options.off");

    private static final MutableInt VALUE = new MutableInt(0);

    public static final SimpleOption<RubyDisplayMode> OPTION = new SimpleOption<>(
        "options.rubi.rubyDisplayMode",
        SimpleOption.emptyTooltip(),
        (optionText, value) -> value.getText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(
            Arrays.asList(RubyDisplayMode.values()),
            Codec.INT.xmap(RubyDisplayMode::byOrdinal, RubyDisplayMode::ordinal)
        ),
        RubyDisplayMode.ABOVE,
        (value) -> {
            Utils.LOGGER.info("Ruby display mode set to {}", value);
            VALUE.setValue(value.ordinal());
        }
    );

    private final Text text;

    RubyDisplayMode(String key) {
        var text = Text.translatable(key);
        var ordered = text.asOrderedText();
        Utils.LOGGER.info(Utils.charsFromOrdered(ordered).toString());
        this.text = Text.translatable(key);
    }

    public static RubyDisplayMode getValue() {
        return byOrdinal(VALUE.getValue());
    }

    public static void accept(GameOptions.Visitor visitor) {
        visitor.accept("rubi.rubyDisplayMode", OPTION);
        VALUE.setValue(OPTION.getValue().ordinal());
    }

    // public static void setValue(RubyMode value) {
    //     OPTION.setValue(value);
    // }

    public static RubyDisplayMode byOrdinal(int ordinal) {
        return RubyDisplayMode.values()[ordinal];
    }

    public Text getText() {
        return this.text;
    }
}
