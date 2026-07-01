package com.leone.kothcaller;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class InventoryImageGenerator {
    public static BufferedImage createInventoryImage(PlayerEntity player) {
        BufferedImage image = new BufferedImage(540, 360, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(new Color(20, 20, 24));
        graphics.fillRect(0, 0, 540, 360);
        graphics.setColor(new Color(70, 70, 80));
        graphics.fillRoundRect(12, 12, 516, 336, 18, 18);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("SansSerif", Font.BOLD, 24));
        graphics.drawString(player.getName().getString() + "'s Inventory", 24, 44);

        graphics.setFont(new Font("SansSerif", Font.PLAIN, 12));
        for (int slot = 0; slot < 36; slot++) {
            int row = slot / 9;
            int col = slot % 9;
            int x = 24 + col * 54;
            int y = 68 + row * 42;

            graphics.setColor(new Color(48, 48, 56));
            graphics.fillRoundRect(x, y, 42, 42, 8, 8);
            graphics.setColor(new Color(150, 150, 160));
            graphics.drawRoundRect(x, y, 42, 42, 8, 8);

            ItemStack stack = player.getInventory().getStack(slot);
            if (!stack.isEmpty()) {
                String label = stack.getName().getString();
                if (label.length() > 12) {
                    label = label.substring(0, 11) + "…";
                }
                graphics.setColor(Color.WHITE);
                graphics.drawString(label, x + 4, y + 18);
                graphics.drawString("x" + stack.getCount(), x + 4, y + 34);
            }
        }

        graphics.dispose();
        return image;
    }
}
