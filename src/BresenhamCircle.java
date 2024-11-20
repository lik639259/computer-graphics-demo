import java.awt.*;
import javax.swing.*;

public class BresenhamCircle extends JPanel {
    private int radius = 0;
    private int centerX, centerY;
    private JTextField radiusInput;
    private JButton drawButton;

    public BresenhamCircle() {
        setPreferredSize(new Dimension(600, 600));
        setLayout(new BorderLayout());
        
        JPanel controlPanel = new JPanel();
        radiusInput = new JTextField(10);
        drawButton = new JButton("绘制");
        controlPanel.add(new JLabel("半径: "));
        controlPanel.add(radiusInput);
        controlPanel.add(drawButton);
        
        add(controlPanel, BorderLayout.NORTH);
        
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                centerX = getWidth() / 2;
                centerY = getHeight() / 2;
                if (radius > 0) {
                    drawCircle(g, centerX, centerY, radius);
                }
            }
        };
        drawingPanel.setPreferredSize(new Dimension(400, 350));
        add(drawingPanel, BorderLayout.CENTER);
        
        drawButton.addActionListener(e -> {
            try {
                radius = Integer.parseInt(radiusInput.getText().trim());
                drawingPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "请输入有效的数字", 
                    "输入错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void drawCircle(Graphics g, int x0, int y0, int r) {
        int x = 0;
        int y = r;
        int d = 3 - 2 * r;

        while (y >= x) {
            g.fillRect(x0 + x, y0 + y, 1, 1);  // 第一象限右上
            g.fillRect(x0 - x, y0 + y, 1, 1);  // 第二象限左上
            g.fillRect(x0 + x, y0 - y, 1, 1);  // 第四象限右下
            g.fillRect(x0 - x, y0 - y, 1, 1);  // 第三象限左下
            g.fillRect(x0 + y, y0 + x, 1, 1);  // 第一象限上右
            g.fillRect(x0 - y, y0 + x, 1, 1);  // 第二象限上左
            g.fillRect(x0 + y, y0 - x, 1, 1);  // 第四象限下右
            g.fillRect(x0 - y, y0 - x, 1, 1);  // 第三象限下左

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
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("圆绘制器");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new BresenhamCircle());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}