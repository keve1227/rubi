package com.kevinsundqvistnorlen.rubi.option;

import com.kevinsundqvistnorlen.rubi.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.*;
import net.minecraft.util.TranslatableOption;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;

public enum RubyRenderMode implements TranslatableOption {
    HIDDEN("hidden"),
    ABOVE("above"),
    BELOW("below"),
    REPLACE("replace");

    private static final MutableInt VALUE = new MutableInt(0);

    public static final SimpleOption<RubyRenderMode> OPTION = new SimpleOption<>(
        "options.rubi.renderMode",
        SimpleOption.emptyTooltip(),
        SimpleOption.enumValueText(),
        new SimpleOption.PotentialValuesBasedCallbacks<>(
            Arrays.asList(RubyRenderMode.values()),
            Codec.INT.xmap(RubyRenderMode::byId, RubyRenderMode::getId)
        ),
        RubyRenderMode.ABOVE,
        (value) -> {
            Utils.LOGGER.info("Ruby display mode changed to " + value.toString() + " (" + value.ordinal() + ")");
            VALUE.setValue(value.ordinal());
        }
    );

    private final String translationKey;

    RubyRenderMode(String name) {
        this.translationKey = "options.rubi.renderMode." + name;
    }

    public static RubyRenderMode getValue() {
        return RubyRenderMode.byId(VALUE.getValue());
    }

    public static void accept(GameOptions.Visitor visitor) {
        visitor.accept("rubi.renderMode", OPTION);
        VALUE.setValue(OPTION.getValue().ordinal());
    }

    // public static void setValue(RubyDisplayMode value) {
    //     OPTION.setValue(value);
    // }

    public static RubyRenderMode byId(int id) {
        return RubyRenderMode.values()[id];
    }

    @Override
    public int getId() {
        return this.ordinal();
    }

    @Override
    public String getTranslationKey() {
        return this.translationKey;
    }
}
