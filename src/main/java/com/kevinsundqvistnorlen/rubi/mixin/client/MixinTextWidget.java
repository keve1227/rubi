package com.kevinsundqvistnorlen.rubi.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractTextWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextWidget.class)
public abstract class MixinTextWidget extends AbstractTextWidget {
    public MixinTextWidget(int x, int y, int width, int height, Text message, TextRenderer textRenderer) {
        super(x, y, width, height, message, textRenderer);
    }

    @Unique
    public int getWidth() {
        return this.getTextRenderer().getWidth(this.getMessage().asOrderedText());
    }
}
