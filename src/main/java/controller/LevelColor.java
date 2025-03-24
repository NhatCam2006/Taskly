package controller;

import javafx.scene.paint.Color;

public class LevelColor {
    private final int level;
    private final Color color;

    public LevelColor(int level, Color color) {
        this.level = level;
        this.color = color;
    }

    public int getLevel() {
        return level;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public String toString() {
        return ""; // Không hiển thị chữ, chỉ hiển thị màu
    }
}