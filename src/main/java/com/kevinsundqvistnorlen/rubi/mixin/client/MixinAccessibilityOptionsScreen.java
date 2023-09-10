package com.kevinsundqvistnorlen.rubi.mixin.client;

import com.kevinsundqvistnorlen.rubi.option.RubyRenderMode;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(AccessibilityOptionsScreen.class)
public class MixinAccessibilityOptionsScreen {

    @Inject(method = "getOptions", at = @At("RETURN"), cancellable = true)
    private static void injectGetOptions(CallbackInfoReturnable<SimpleOption<?>[]> info) {
        SimpleOption<?>[] options = info.getReturnValue();
        SimpleOption<?>[] newOptions = Arrays.copyOf(options, options.length + 1);
        newOptions[options.length] = RubyRenderMode.OPTION;
        info.setReturnValue(newOptions);
    }
}
