package xyz.chengzi.aeroplanechess.view;

import javax.swing.*;
import java.awt.*;

public class SquareComponent extends JPanel {
    private final Color color;
    private final int player;
    private final int index;
    public int[] shortCutIndex = {3, 6, 9, 12};

    public SquareComponent(int size, Color color, int player, int index) {
        setLayout(new GridLayout(1, 1)); // Use 1x1 grid layout
        setSize(size, size);
        this.color = color;
        this.player = player;
        this.index = index;
    }

    public Color getColor() {
        return color;
    }

    public int getPlayer() {
        return player;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintSquare(g);
    }

    private void paintSquare(Graphics g) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(color);
        g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);

        g.setColor(Color.WHITE);
        g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);

        for (int i = 0; i < shortCutIndex.length; i++) {
            if (shortCutIndex[i] == this.getIndex()) {
                g.setColor(color.brighter());
                int x[] = {getWidth() / 2, getWidth(), getWidth() / 2, 0};
                int y[] = {0, getWidth() / 2, getWidth(), getWidth() / 2};
                g.drawPolygon(x, y, 4);
                g.fillPolygon(x, y, 4);
            }
        }

        g.setColor(Color.BLACK);
        g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
}
