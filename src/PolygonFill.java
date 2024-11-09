import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

public class PolygonFill extends JPanel {
    private List<Point> points = new ArrayList<>();
    private Color fillColor = Color.RED;
    private boolean isDrawing = false;
    private boolean isFilled = false;

    public PolygonFill() {
        setPreferredSize(new Dimension(800, 600));
        setupUI();
        setupMouseListeners();
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        JPanel controlPanel = new JPanel();
        
        JButton fillButton = new JButton("填充");
        fillButton.addActionListener(e -> {
            isFilled = true;
            repaint();
        });
        
        JButton colorButton = new JButton("选择颜色");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "选择填充颜色", fillColor);
            if (newColor != null) {
                fillColor = newColor;
                if (isFilled) {
                    repaint();
                }
            }
        });
        
        JButton clearButton = new JButton("清除");
        clearButton.addActionListener(e -> {
            points.clear();
            isFilled = false;
            repaint();
        });

        controlPanel.add(fillButton);
        controlPanel.add(colorButton);
        controlPanel.add(clearButton);
        add(controlPanel, BorderLayout.NORTH);
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!isDrawing) {
                        points.clear();
                        isFilled = false;
                        isDrawing = true;
                    }
                    points.add(e.getPoint());
                    repaint();
                } else if (SwingUtilities.isRightMouseButton(e) && points.size() > 2) {
                    isDrawing = false;
                    points.add(points.get(0)); // 闭合多边形
                    repaint();
                }
            }
        };
        addMouseListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制多边形边界
        if (points.size() > 1) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // 填充多边形
        if (isFilled && points.size() > 2) {
            g2d.setColor(fillColor);
            scanLineFill(g2d);
        }
    }

    private void scanLineFill(Graphics2D g) {
        // 找到多边形的边界
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Point p : points) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // 对每一条扫描线
        for (int y = minY; y <= maxY; y++) {
            List<Integer> intersections = new ArrayList<>();
            
            // 计算扫描线与多边形边的交点
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                
                // 确保p1的y坐标小于p2的y坐标
                if (p1.y > p2.y) {
                    Point temp = p1;
                    p1 = p2;
                    p2 = temp;
                }
                
                // 如果扫描线与边相交
                if (y >= p1.y && y < p2.y && p1.y != p2.y) {
                    // 计算交点的x坐标
                    int x = p1.x + (y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y);
                    intersections.add(x);
                }
            }
            
            // 对交点进行排序
            Collections.sort(intersections);
            
            // 两两配对，填充线段
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                if (i + 1 < intersections.size()) {
                    int x1 = intersections.get(i);
                    int x2 = intersections.get(i + 1);
                    g.drawLine(x1, y, x2, y);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("多边形填充");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new PolygonFill());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 