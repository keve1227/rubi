package com.kevinsundqvistnorlen.rubi.mixin.client;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(TextVisitFactory.class)
public class MixinTextVisitFactory {

    @ModifyVariable(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;" +
                    "Lnet/minecraft/text/CharacterVisitor;)Z", at = @At("HEAD"), argsOnly = true
    )
    private static String visitFormatted(String string) {
        return string.replaceAll("[\ue9c0-\ue9c2]", "")
                .replaceAll("§\\^\\s*(.+?)\\s*\\(\\s*(.+?)\\s*\\)", "\ue9c0$1\ue9c1$2\ue9c2");
    }
}