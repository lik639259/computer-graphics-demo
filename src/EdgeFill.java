import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class EdgeFill extends JPanel {
    private List<Point> points = new ArrayList<>();
    private Color fillColor = Color.RED;
    private boolean isDrawing = false;
    private boolean isFilled = false;

    // 定义边结构
    private static class Edge {
        int yMax;      // 边的最大y值
        float xMin;    // 当前扫描线与边的交点x值
        float dx;      // 斜率的倒数
        
        Edge(int yMax, float xMin, float dx) {
            this.yMax = yMax;
            this.xMin = xMin;
            this.dx = dx;
        }
    }

    public EdgeFill() {
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
            g2d.setColor(Color.BLACK);
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // 填充多边形
        if (isFilled && points.size() > 2) {
            g2d.setColor(fillColor);
            edgeFill(g2d);
        }
    }

    private void edgeFill(Graphics2D g) {
        // 找到多边形的边界
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Point p : points) {
            minY = Math.min(minY, p.y);
            maxY = Math.max(maxY, p.y);
        }

        // 创建边表
        @SuppressWarnings("unchecked")
        ArrayList<Edge>[] ET = (ArrayList<Edge>[]) new ArrayList[maxY + 1];
        for (int i = 0; i <= maxY; i++) {
            ET[i] = new ArrayList<>();
        }

        // 构建边表
        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            
            // 忽略水平边
            if (p1.y == p2.y) continue;
            
            // 确保p1的y坐标小于p2的y坐标
            if (p1.y > p2.y) {
                Point temp = p1;
                p1 = p2;
                p2 = temp;
            }
            
            // 计算斜率的倒数
            float dx = (float)(p2.x - p1.x) / (p2.y - p1.y);
            
            // 创建新边并添加到边表
            Edge edge = new Edge(p2.y, p1.x, dx);
            ET[p1.y].add(edge);
        }

        // 活动边表
        ArrayList<Edge> AET = new ArrayList<>();

        // 扫描线算法
        for (int y = minY; y <= maxY; y++) {
            // 将新边加入AET
            AET.addAll(ET[y]);
            
            // 移除yMax = y的边
            final int currentY = y;
            AET.removeIf(edge -> edge.yMax == currentY);
            
            // 按x值排序
            AET.sort((e1, e2) -> Float.compare(e1.xMin, e2.xMin));
            
            // 填充扫描线
            for (int i = 0; i < AET.size() - 1; i += 2) {
                if (i + 1 < AET.size()) {
                    int x1 = Math.round(AET.get(i).xMin);
                    int x2 = Math.round(AET.get(i + 1).xMin);
                    g.drawLine(x1, y, x2, y);
                }
            }
            
            // 更新x值
            for (Edge edge : AET) {
                edge.xMin += edge.dx;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("多边形填充");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new EdgeFill());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
} 