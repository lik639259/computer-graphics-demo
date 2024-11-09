import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class BresenhamLine extends JPanel {
    private Point p1 = null; 
    private Point p2 = null; 

    public BresenhamLine() {
        setPreferredSize(new Dimension(400, 400));
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    p2 = e.getPoint();
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (p1 == null) {
            p1 = new Point(getWidth() / 2, getHeight() / 2);
        }
        if (p2 != null) {
            drawLine(g, p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawLine(Graphics g, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2;

        while (true) {
            g.drawLine(x0, y0, x0, y0);
            if (x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Bresenham Line Drawing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new BresenhamLine());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}