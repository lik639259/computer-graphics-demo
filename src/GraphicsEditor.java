import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class GraphicsEditor extends JFrame {
    private DrawingPanel drawingPanel;
    private JTextField sizeField;
    private List<Shape> shapes = new ArrayList<>();
    private Shape currentShape = null;
    private int shapeType = 0; // 0:正方形, 1:六边形, 2:五角星
    private int transformMode = 0; // 0:绘制, 1:平移, 2:旋转, 3:缩放, 4:错切
    private Point anchorPoint = null;
    private Point lastPoint = null;
    private double lastAngle = 0;
    private boolean isDrawing = false;
    private boolean isDragging = false;
    private Point startPoint = null;
    private Shape previewShape = null;

    public GraphicsEditor() {
        setTitle("图形编辑器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建工具栏
        JToolBar toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // 创建绘图面板
        drawingPanel = new DrawingPanel();
        add(drawingPanel, BorderLayout.CENTER);
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // 图形选择
        String[] shapes = {"正方形", "正六边形", "五角星"};
        JComboBox<String> shapeCombo = new JComboBox<>(shapes);
        shapeCombo.addActionListener(e -> {
            shapeType = shapeCombo.getSelectedIndex();
        });

        // 尺寸输入
        sizeField = new JTextField("100", 5);

        // 绘制按钮
        JButton drawButton = new JButton("绘制");
        drawButton.addActionListener(e -> {
            // 使用输入的尺寸直接创建图形
            int size = Integer.parseInt(sizeField.getText());
            Point center = new Point(drawingPanel.getWidth()/2, drawingPanel.getHeight()/2);
            createShapeWithSize(center, size);
        });

        // 变换按钮
        JButton[] buttons = {
            new JButton("平移"),
            new JButton("旋转"),
            new JButton("缩放"),
            new JButton("错切")
        };

        for (int i = 0; i < buttons.length; i++) {
            final int mode = i + 1;
            buttons[i].addActionListener(e -> {
                transformMode = mode;
                isDrawing = false;
            });
        }

        // 添加组件到工具栏
        toolbar.add(new JLabel("图形: "));
        toolbar.add(shapeCombo);
        toolbar.add(new JLabel("大小: "));
        toolbar.add(sizeField);
        toolbar.add(drawButton);
        toolbar.addSeparator();
        for (JButton btn : buttons) {
            toolbar.add(btn);
        }

        return toolbar;
    }

    class DrawingPanel extends JPanel {
        public DrawingPanel() {
            setBackground(Color.WHITE);
            setupMouseListeners();
        }

        private void setupMouseListeners() {
            MouseAdapter mouseHandler = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (transformMode == 0) {
                            // 开始拖拽绘制
                            startPoint = e.getPoint();
                            isDragging = true;
                        } else {
                            // 选择图形进行变换
                            selectShape(e.getPoint());
                        }
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        // 设置变换锚点
                        anchorPoint = e.getPoint();
                        if (transformMode == 2) {
                            lastAngle = getAngle(anchorPoint, e.getPoint());
                        }
                    }
                    lastPoint = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging && startPoint != null) {
                        // 完成拖拽绘制
                        if (previewShape != null) {
                            shapes.add(previewShape);
                            currentShape = previewShape;
                            previewShape = null;
                        }
                        startPoint = null;
                        isDragging = false;
                    }
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    Point currentPoint = e.getPoint();
                    if (isDragging && startPoint != null) {
                        // 预览拖拽绘制的图形
                        updatePreviewShape(currentPoint);
                    } else if (currentShape != null && lastPoint != null && transformMode > 0) {
                        // 变换操作
                        switch (transformMode) {
                            case 1: translate(currentPoint); break;
                            case 2: rotate(currentPoint); break;
                            case 3: scale(currentPoint); break;
                            case 4: shear(currentPoint); break;
                        }
                    }
                    lastPoint = currentPoint;
                    repaint();
                }
            };

            addMouseListener(mouseHandler);
            addMouseMotionListener(mouseHandler);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制所有图形
            for (Shape shape : shapes) {
                if (shape == currentShape) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                shape.draw(g2d);
            }

            // 绘制预览图形
            if (previewShape != null) {
                g2d.setColor(Color.GRAY);
                previewShape.draw(g2d);
            }

            // 绘制锚点
            if (anchorPoint != null) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(anchorPoint.x - 3, anchorPoint.y - 3, 6, 6);
            }
        }
    }

    // 图形类
    class Shape {
        private List<Point2D> vertices;
        
        public Shape(List<Point2D> vertices) {
            this.vertices = vertices;
        }
        
        public void draw(Graphics2D g) {
            int[] xPoints = new int[vertices.size()];
            int[] yPoints = new int[vertices.size()];
            
            for (int i = 0; i < vertices.size(); i++) {
                xPoints[i] = (int) vertices.get(i).x;
                yPoints[i] = (int) vertices.get(i).y;
            }
            
            g.drawPolygon(xPoints, yPoints, vertices.size());
        }
        
        public boolean contains(Point p) {
            Polygon poly = new Polygon();
            for (Point2D vertex : vertices) {
                poly.addPoint((int)vertex.x, (int)vertex.y);
            }
            return poly.contains(p);
        }
        
        public List<Point2D> getVertices() {
            return vertices;
        }
    }

    // 点类
    class Point2D {
        double x, y;
        
        Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    // 使用指定尺寸创建图形
    private void createShapeWithSize(Point center, int size) {
        List<Point2D> vertices = new ArrayList<>();
        switch (shapeType) {
            case 0: vertices = createSquare(size); break;
            case 1: vertices = createHexagon(size); break;
            case 2: vertices = createStar(size); break;
        }
        
        // 移动到指定位置
        for (Point2D p : vertices) {
            p.x += center.x;
            p.y += center.y;
        }
        
        Shape shape = new Shape(vertices);
        shapes.add(shape);
        currentShape = shape;
        repaint();
    }

    private List<Point2D> createSquare(int size) {
        List<Point2D> points = new ArrayList<>();
        double half = size / 2.0;
        points.add(new Point2D(-half, -half));
        points.add(new Point2D(half, -half));
        points.add(new Point2D(half, half));
        points.add(new Point2D(-half, half));
        return points;
    }

    private List<Point2D> createHexagon(int size) {
        List<Point2D> points = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3;
            points.add(new Point2D(
                size * Math.cos(angle),
                size * Math.sin(angle)
            ));
        }
        return points;
    }

    private List<Point2D> createStar(int size) {
        List<Point2D> points = new ArrayList<>();
        double startAngle = -Math.PI / 2;
        for (int i = 0; i < 10; i++) {
            double angle = startAngle + i * Math.PI / 5;
            double r = (i % 2 == 0) ? size : size * 0.4;
            points.add(new Point2D(
                r * Math.cos(angle),
                r * Math.sin(angle)
            ));
        }
        return points;
    }

    // 变换方法
    private void translate(Point currentPoint) {  // 平移
        if (currentShape == null || lastPoint == null) return;
        
        double dx = currentPoint.x - lastPoint.x;
        double dy = currentPoint.y - lastPoint.y;
        
        double[][] matrix = {
            {1, 0, dx},
            {0, 1, dy},
            {0, 0, 1}
        };
        
        transform(matrix);
    }

    private void rotate(Point currentPoint) { // 旋转
        if (currentShape == null || anchorPoint == null) return;
        
        double currentAngle = getAngle(anchorPoint, currentPoint);
        double deltaAngle = currentAngle - lastAngle;
        
        double sin = Math.sin(deltaAngle);
        double cos = Math.cos(deltaAngle);
        
        double[][] matrix = {
            {cos, -sin, anchorPoint.x * (1 - cos) + anchorPoint.y * sin},
            {sin, cos, anchorPoint.y * (1 - cos) - anchorPoint.x * sin},
            {0, 0, 1}
        };
        
        transform(matrix);
        lastAngle = currentAngle;
    }

    private void scale(Point currentPoint) { // 缩放
        if (currentShape == null || anchorPoint == null || lastPoint == null) return;
        
        double startDist = distance(lastPoint, anchorPoint);
        double currentDist = distance(currentPoint, anchorPoint);
        double scale = currentDist / startDist;
        
        double[][] matrix = {
            {scale, 0, anchorPoint.x * (1 - scale)},
            {0, scale, anchorPoint.y * (1 - scale)},
            {0, 0, 1}
        };
        
        transform(matrix);
    }

    private void shear(Point currentPoint) { // 错切
        if (currentShape == null || anchorPoint == null || lastPoint == null) return;
        
        double dx = (currentPoint.x - lastPoint.x) / 100.0;
        double dy = (currentPoint.y - lastPoint.y) / 100.0;
        
        double[][] matrix = {
            {1, dx, -anchorPoint.y * dx},
            {dy, 1, -anchorPoint.x * dy},
            {0, 0, 1}
        };
        
        transform(matrix);
    }

    private void transform(double[][] matrix) {
        for (Point2D p : currentShape.getVertices()) {
            double[] point = {p.x, p.y, 1};
            double[] result = new double[3];
            
            for (int i = 0; i < 3; i++) {
                result[i] = 0;
                for (int j = 0; j < 3; j++) {
                    result[i] += matrix[i][j] * point[j];
                }
            }
            
            p.x = result[0];
            p.y = result[1];
        }
    }

    // 辅助方法
    private void selectShape(Point p) { // 选择图形
        currentShape = null;
        for (Shape shape : shapes) {
            if (shape.contains(p)) {
                currentShape = shape;
                break;
            }
        }
    }

    private double getAngle(Point center, Point p) { // 获取角度
        return Math.atan2(p.y - center.y, p.x - center.x);
    }

    private double distance(Point p1, Point p2) { // 获取距离
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    // 更新拖拽预览
    private void updatePreviewShape(Point currentPoint) { // 更新拖拽预览
        if (startPoint == null) return;
        
        // 计算拖拽距离作为大小
        int size = (int) Math.sqrt(
            Math.pow(currentPoint.x - startPoint.x, 2) +
            Math.pow(currentPoint.y - startPoint.y, 2)
        );
        
        List<Point2D> vertices = new ArrayList<>();
        switch (shapeType) {
            case 0: vertices = createSquare(size); break;
            case 1: vertices = createHexagon(size); break;
            case 2: vertices = createStar(size); break;
        }
        
        // 移动到起始点
        for (Point2D p : vertices) {
            p.x += startPoint.x;
            p.y += startPoint.y;
        }
        
        previewShape = new Shape(vertices);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GraphicsEditor().setVisible(true);
        });
    }
} 