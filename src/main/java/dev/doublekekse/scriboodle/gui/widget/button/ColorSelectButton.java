package dev.doublekekse.scriboodle.gui.widget.button;

import dev.doublekekse.scriboodle.gui.widget.ScribbleArea;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public class ColorSelectButton extends ScribbleItemButton {
    DyeColor dye;

    public ColorSelectButton(int x, int y, Component message, ItemStack stack, DyeColor dye, ScribbleArea scribbleArea) {
        this.dye = dye;

        super(x, y, message, _ -> {
            if (!Minecraft.getInstance().hasShiftDown()) {
                scribbleArea.colors.clear();
            }

            if (scribbleArea.colors.contains(dye)) {
                scribbleArea.colors.remove(dye);
            } else {
                scribbleArea.colors.add(dye);
            }
            scribbleArea.updateColor();
        }, stack, scribbleArea);
    }

    @Override
    protected boolean outlineVisible() {
        return scribbleArea.colors.contains(dye);
    }
}
