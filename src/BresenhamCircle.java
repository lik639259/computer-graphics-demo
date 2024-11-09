import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class BresenhamCircle extends JPanel {
    private int radius = 0; // 圆的半径
    private int centerX, centerY; // 圆心坐标

    public BresenhamCircle() {
        setPreferredSize(new Dimension(400, 400));
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    radius = Math.max(Math.abs(e.getX() - centerX), Math.abs(e.getY() - centerY));
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        if (radius > 0) {
            drawCircle(g, centerX, centerY, radius);
        }
    }

    private void drawCircle(Graphics g, int x0, int y0, int r) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        while (y >= x) {
            // 8个对称点
            g.drawLine(x0 + x, y0 + y, x0 + x, y0 + y);
            g.drawLine(x0 - x, y0 + y, x0 - x, y0 + y);
            g.drawLine(x0 + x, y0 - y, x0 + x, y0 - y);
            g.drawLine(x0 - x, y0 - y, x0 - x, y0 - y);
            g.drawLine(x0 + y, y0 + x, x0 + y, y0 + x);
            g.drawLine(x0 - y, y0 + x, x0 - y, y0 + x);
            g.drawLine(x0 + y, y0 - x, x0 + y, y0 - x);
            g.drawLine(x0 - y, y0 - x, x0 - y, y0 - x);

            if (d < 0) {
                d += 4 * x + 6;
            } else {
                d += 4 * (x - y) + 10;
                y--;
            }
            x++;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bresenham Circle Drawing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new BresenhamCircle());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}