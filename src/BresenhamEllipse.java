import java.awt.*;
import javax.swing.*;

public class BresenhamEllipse extends JFrame {
    private JTextField aField, bField;
    private DrawPanel drawPanel;
    
    public BresenhamEllipse() {
        setTitle("椭圆绘制器");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // 创建输入面板
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("长轴 a:"));
        aField = new JTextField(5);
        inputPanel.add(aField);
        inputPanel.add(new JLabel("短轴 b:"));
        bField = new JTextField(5);
        inputPanel.add(bField);
        
        JButton drawButton = new JButton("绘制");
        drawButton.addActionListener(e -> {
            try {
                int a = Integer.parseInt(aField.getText());
                int b = Integer.parseInt(bField.getText());
                drawPanel.setParameters(a, b);
                drawPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字");
            }
        });
        inputPanel.add(drawButton);
        
        // 创建绘图面板
        drawPanel = new DrawPanel();
        
        // 布局
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(drawPanel, BorderLayout.CENTER);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BresenhamEllipse().setVisible(true);
        });
    }
}

class DrawPanel extends JPanel {
    private int a = 0, b = 0;
    
    public void setParameters(int a, int b) {
        this.a = a;
        this.b = b;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (a <= 0 || b <= 0) return;
        
        // 获取面板中心点
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        
        // 使用Bresenham算法绘制椭圆
        drawBresenhamEllipse(g, centerX, centerY, a, b);
    }
    
    private void drawBresenhamEllipse(Graphics g, int centerX, int centerY, int a, int b) {
        int x = 0;
        int y = b;
        
        // 区域1的初始决策参数
        long d1 = b * b - a * a * b + a * a / 4;
        int dx = 2 * b * b * x;
        int dy = 2 * a * a * y;
        
        // 绘制区域1的点
        while (dx < dy) {
            plotEllipsePoints(g, centerX, centerY, x, y);
            
            x++;
            dx += 2 * b * b;
            if (d1 < 0) {
                d1 += dx + b * b;
            } else {
                y--;
                dy -= 2 * a * a;
                d1 += dx - dy + b * b;
            }
        }
        
        // 区域2的初始决策参数
        long d2 = b * b * (x + 1) * (x + 1) + a * a * (y - 1) * (y - 1) - a * a * b * b;
        
        // 绘制区域2的点
        while (y >= 0) {
            plotEllipsePoints(g, centerX, centerY, x, y);
            
            y--;
            dy -= 2 * a * a;
            if (d2 > 0) {
                d2 += a * a - dy;
            } else {
                x++;
                dx += 2 * b * b;
                d2 += dx - dy + a * a;
            }
        }
    }
    
    private void plotEllipsePoints(Graphics g, int centerX, int centerY, int x, int y) {
        // 由于椭圆的对称性，一次画出八个点
        g.drawLine(centerX + x, centerY + y, centerX + x, centerY + y);
        g.drawLine(centerX - x, centerY + y, centerX - x, centerY + y);
        g.drawLine(centerX + x, centerY - y, centerX + x, centerY - y);
        g.drawLine(centerX - x, centerY - y, centerX - x, centerY - y);
    }
} 