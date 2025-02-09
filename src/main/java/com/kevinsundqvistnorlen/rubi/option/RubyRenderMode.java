package com.kevinsundqvistnorlen.rubi.option;

import com.kevinsundqvistnorlen.rubi.Utils;
import com.mojang.serialization.Codec;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.util.TranslatableOption;

import java.util.Arrays;

public enum RubyRenderMode implements TranslatableOption {
    HIDDEN("hidden"),
    ABOVE("above"),
    BELOW("below"),
    REPLACE("replace");

    private final String translationKey;

    RubyRenderMode(String name) {
        this.translationKey = Option.TRANSLATION_KEY + "." + name;
    }

    public static void accept(GameOptions.Visitor visitor) {
        visitor.accept("rubi.renderMode", Option.INSTANCE);
    }

    public static SimpleOption<RubyRenderMode> getOption() {
        return Option.INSTANCE;
    }

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

    private static final class Option {
        static final String TRANSLATION_KEY = "options.rubi.renderMode";
        static final SimpleOption<RubyRenderMode> INSTANCE = new SimpleOption<>(
            TRANSLATION_KEY, SimpleOption.emptyTooltip(), SimpleOption.enumValueText(),
            new SimpleOption.PotentialValuesBasedCallbacks<>(
                Arrays.asList(RubyRenderMode.values()),
                Codec.INT.xmap(RubyRenderMode::byId, RubyRenderMode::getId)
            ), RubyRenderMode.ABOVE,
            (value) -> Utils.LOGGER.debug("Ruby display mode changed to {} ({})", value.toString(), value.ordinal())
        );
    }
}
