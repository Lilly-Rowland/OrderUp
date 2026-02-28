package com.example.game;

/**
 * Simple data holder for a menu item.
 */
public class MenuItem {
    public final String name;
    public final double price;
    public final double quality; // 0.0 - 1.0

    public MenuItem(String name, double price, double quality) {
        this.name = name;
        this.price = price;
        this.quality = Math.max(0.0, Math.min(1.0, quality));
    }
}
