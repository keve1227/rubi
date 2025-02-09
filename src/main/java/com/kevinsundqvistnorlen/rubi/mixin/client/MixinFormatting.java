package com.kevinsundqvistnorlen.rubi.mixin.client;

import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.regex.Pattern;

@Mixin(Formatting.class)
public abstract class MixinFormatting {
    @SuppressWarnings("unused")
    @Shadow private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR^]");
}
