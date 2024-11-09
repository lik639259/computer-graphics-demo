import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ShapeDrawer extends JFrame {
    private int shapeType = 0; // 0-正方形, 1-正六边形, 2-五角星
    private Point startPoint;
    private Point endPoint;
    private Shape currentShape;
    private JTextField sizeInput;
    private int inputSize = 100;
    private DrawingPanel drawingPanel;
    private List<Shape> savedShapes = new ArrayList<>();
    private boolean isTransforming = false;
    private int transformType = -1; // 0-平移, 1-旋转, 2-缩放, 3-错切
    private Point transformStart;
    private Shape selectedShape;
    private boolean isDrawingMode = true; // 添加绘图模式标志
    private Point translationStart;
    private Point translationEnd;
    private boolean waitingForSecondClick = false;
    private Point fixedPoint; // 用于存储不动点（旋转中心点或缩放基准点）
    private boolean waitingForFixedPoint = false; // 是否等待设置不动点
    private boolean waitingForEndPoint = false; // 是否等待设置终点
    private double lastAngle; // 用于存储上一次的旋转角度
    private boolean isShearX = true; // 用于标识是水平错切还是垂直错切

    public ShapeDrawer() {
        setTitle("图形绘制器");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建工具栏和输入区域
        JToolBar toolBar = new JToolBar();
        JButton squareBtn = new JButton("正方形");
        JButton hexagonBtn = new JButton("正六边形");
        JButton starBtn = new JButton("五角星");
        
        // 添加尺寸输入组件
        JLabel sizeLabel = new JLabel("尺寸:");
        sizeInput = new JTextField("100", 5);
        JButton drawBtn = new JButton("绘制");
        
        JButton saveBtn = new JButton("保存");
        JButton clearBtn = new JButton("清除");
        JButton translateBtn = new JButton("平移");
        JButton rotateBtn = new JButton("旋转");
        JButton scaleBtn = new JButton("缩放");
        JButton shearBtn = new JButton("错切");
        
        toolBar.add(squareBtn);
        toolBar.add(hexagonBtn);
        toolBar.add(starBtn);
        toolBar.add(sizeLabel);
        toolBar.add(sizeInput);
        toolBar.add(drawBtn);
        toolBar.add(saveBtn);
        toolBar.add(clearBtn);
        toolBar.add(translateBtn);
        toolBar.add(rotateBtn);
        toolBar.add(scaleBtn);
        toolBar.add(shearBtn);
        
        // 添加按钮事件监听
        squareBtn.addActionListener(e -> shapeType = 0);
        hexagonBtn.addActionListener(e -> shapeType = 1);
        starBtn.addActionListener(e -> shapeType = 2);

        // 添加绘制按钮事件监听
        drawBtn.addActionListener(e -> {
            try {
                inputSize = Integer.parseInt(sizeInput.getText());
                startPoint = new Point(getWidth()/3, getHeight()/3);
                endPoint = new Point(startPoint.x + inputSize, startPoint.y + inputSize);
                drawingPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的数字！");
            }
        });

        saveBtn.addActionListener(e -> {
            if (currentShape != null) {
                savedShapes.add(currentShape);
                currentShape = null;
                startPoint = null;
                endPoint = null;
                drawingPanel.repaint();
            }
        });

        clearBtn.addActionListener(e -> {
            savedShapes.clear();
            currentShape = null;
            startPoint = null;
            endPoint = null;
            isDrawingMode = true; // 清除后恢复绘图模式
            transformType = -1; // 重置变换类型
            drawingPanel.repaint();
        });

        translateBtn.addActionListener(e -> transformType = 0);
        rotateBtn.addActionListener(e -> transformType = 1);
        scaleBtn.addActionListener(e -> transformType = 2);
        shearBtn.addActionListener(e -> transformType = 3);

        // 创建绘图面板
        drawingPanel = new DrawingPanel();
        
        add(toolBar, BorderLayout.NORTH);
        add(drawingPanel, BorderLayout.CENTER);
    }

    class DrawingPanel extends JPanel {
        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (transformType != -1) {  // 变换模式
                        if (e.getButton() == MouseEvent.BUTTON3) { // 鼠标右键
                            switch (transformType) {
                                case 0: // 平移
                                    handleTranslationClick(e.getPoint());
                                    break;
                                case 1: // 旋转
                                    if (!waitingForFixedPoint) {
                                        fixedPoint = e.getPoint();
                                        waitingForFixedPoint = true;
                                        lastAngle = 0;
                                    }
                                    break;
                                case 2: // 缩放
                                    handleScaleClick(e.getPoint());
                                    break;
                                case 3: // 错切
                                    if (!waitingForFixedPoint) {
                                        fixedPoint = e.getPoint();
                                        waitingForFixedPoint = true;
                                        isShearX = true; // 默认先进行水平错切
                                    } else {
                                        Point endPoint = e.getPoint();
                                        if (selectedShape != null) {
                                            double shearFactor;
                                            if (isShearX) {
                                                shearFactor = (endPoint.x - fixedPoint.x) / 100.0;
                                                double[][] matrix = new double[][]{
                                                    {1, shearFactor, 0},
                                                    {0, 1, 0},
                                                    {0, 0, 1}
                                                };
                                                transform(selectedShape, matrix);
                                            } else {
                                                shearFactor = (endPoint.y - fixedPoint.y) / 100.0;
                                                double[][] matrix = new double[][]{
                                                    {1, 0, 0},
                                                    {shearFactor, 1, 0},
                                                    {0, 0, 1}
                                                };
                                                transform(selectedShape, matrix);
                                            }
                                        }
                                        isShearX = !isShearX;
                                        if (isShearX) {
                                            waitingForFixedPoint = false;
                                            fixedPoint = null;
                                        }
                                    }
                                    break;
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON1) { // 鼠标左键
                            // 选择要变换的图形
                            Point2D p = e.getPoint();
                            selectedShape = null;
                            for (Shape shape : savedShapes) {
                                if (shape.contains(p)) {
                                    selectedShape = shape;
                                    break;
                                }
                            }
                        }
                    } else {  // 绘图模式
                        startPoint = e.getPoint();
                        endPoint = e.getPoint();
                    }
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (transformType == 1 || transformType == 3) { // 旋转或错切
                        isTransforming = false;
                    }
                    repaint();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!isDrawingMode && selectedShape != null) {
                        Point current = e.getPoint();
                        
                        switch (transformType) {
                            case 1: // 旋转
                                if (waitingForFixedPoint && fixedPoint != null) {
                                    // 计算相对于固定点的角度
                                    double angle = Math.atan2(current.y - fixedPoint.y, 
                                                            current.x - fixedPoint.x);
                                    double deltaAngle = angle - lastAngle;
                                    lastAngle = angle;
                                    
                                    double[][] matrix = new double[][]{
                                        {Math.cos(deltaAngle), -Math.sin(deltaAngle), 
                                         fixedPoint.x * (1 - Math.cos(deltaAngle)) + fixedPoint.y * Math.sin(deltaAngle)},
                                        {Math.sin(deltaAngle), Math.cos(deltaAngle), 
                                         fixedPoint.y * (1 - Math.cos(deltaAngle)) - fixedPoint.x * Math.sin(deltaAngle)},
                                        {0, 0, 1}
                                    };
                                    transform(selectedShape, matrix);
                                }
                                break;
                            case 3: // 错切
                                // 计算相对于初始位置的错切量
                                double shearX = (current.x - transformStart.x) / 100.0;
                                double shearY = (current.y - transformStart.y) / 100.0;
                                
                                double[][] matrix = new double[][]{
                                    {1, shearX, 0},
                                    {shearY, 1, 0},
                                    {0, 0, 1}
                                };
                                transform(selectedShape, matrix);
                                transformStart = current;
                                break;
                        }
                        repaint();
                    } else if (isDrawingMode) {
                        endPoint = e.getPoint();
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制已保存的图形
            g2d.setColor(Color.RED);
            for (Shape shape : savedShapes) {
                g2d.draw(shape);
            }

            // 绘制当前图形
            g2d.setColor(Color.BLACK);
            if (startPoint != null && endPoint != null && isDrawingMode) {
                int size;
                if (endPoint.x == startPoint.x + inputSize && 
                    endPoint.y == startPoint.y + inputSize) {
                    size = inputSize;
                } else {
                    size = (int) Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + 
                                         Math.pow(endPoint.y - startPoint.y, 2));
                }
                
                switch (shapeType) {
                    case 0: // 正方形
                        currentShape = createSquare(startPoint.x, startPoint.y, size);
                        break;
                    case 1: // 正六边形
                        currentShape = createHexagon(startPoint.x, startPoint.y, size);
                        break;
                    case 2: // 五角星
                        currentShape = createStar(startPoint.x, startPoint.y, size);
                        break;
                }
                g2d.draw(currentShape);
            }

            // 绘制变换参考点和提示信息
            if (!isDrawingMode && fixedPoint != null) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(fixedPoint.x - 5, fixedPoint.y - 5, 10, 10);
                
                String message = "";
                switch (transformType) {
                    case 1: // 旋转
                        message = "拖动鼠标进行旋转";
                        break;
                    case 2: // 缩放
                        message = "点击右键确定缩放终点";
                        break;
                    case 3: // 错切
                        message = isShearX ? "点击右键确定水平错切终点" : "点击右键确定垂直错切终点";
                        break;
                }
                g2d.drawString(message, fixedPoint.x + 10, fixedPoint.y);
            }

            // 绘制平移参考点
            if (transformType == 0 && translationStart != null) {
                g2d.setColor(Color.BLUE);
                g2d.fillOval(translationStart.x - 5, translationStart.y - 5, 10, 10);
                if (waitingForSecondClick) {
                    g2d.drawString("请点击右键选择终点", translationStart.x + 10, translationStart.y);
                }
            }
        }

        private Shape createSquare(int x, int y, int size) {
            return new Rectangle2D.Double(x, y, size, size);
        }

        private Shape createHexagon(int x, int y, int size) {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < 6; i++) {
                double angle = i * Math.PI / 3;
                double xPoint = x + size * Math.cos(angle);
                double yPoint = y + size * Math.sin(angle);
                if (i == 0) {
                    path.moveTo(xPoint, yPoint);
                } else {
                    path.lineTo(xPoint, yPoint);
                }
            }
            path.closePath();
            return path;
        }

        private Shape createStar(int x, int y, int size) {
            Path2D path = new Path2D.Double();
            double startAngle = -Math.PI / 2;
            for (int i = 0; i < 10; i++) {
                double angle = startAngle + i * Math.PI / 5;
                int radius = (i % 2 == 0) ? size : size / 2;
                double xPoint = x + radius * Math.cos(angle);
                double yPoint = y + radius * Math.sin(angle);
                if (i == 0) {
                    path.moveTo(xPoint, yPoint);
                } else {
                    path.lineTo(xPoint, yPoint);
                }
            }
            path.closePath();
            return path;
        }

        // 添加矩阵变换方法
        private void transform(Shape shape, double[][] matrix) {
            int index = savedShapes.indexOf(shape);
            if (index >= 0) {
                Path2D newPath = new Path2D.Double();
                PathIterator it = shape.getPathIterator(null);
                double[] coords = new double[6];
                
                while (!it.isDone()) {
                    int type = it.currentSegment(coords);
                    double[] point = new double[]{coords[0], coords[1], 1};
                    double[] newPoint = matrixMultiply(matrix, point);
                    
                    switch (type) {
                        case PathIterator.SEG_MOVETO:
                            newPath.moveTo(newPoint[0], newPoint[1]);
                            break;
                        case PathIterator.SEG_LINETO:
                            newPath.lineTo(newPoint[0], newPoint[1]);
                            break;
                        case PathIterator.SEG_CLOSE:
                            newPath.closePath();
                            break;
                    }
                    it.next();
                }
                savedShapes.set(index, newPath);
            }
        }

        private double[] matrixMultiply(double[][] matrix, double[] point) {
            double[] result = new double[3];
            for (int i = 0; i < 3; i++) {
                result[i] = 0;
                for (int j = 0; j < 3; j++) {
                    result[i] += matrix[i][j] * point[j];
                }
            }
            return new double[]{result[0], result[1]};
        }

        // 添加处理平移点击的方法
        private void handleTranslationClick(Point point) {
            if (!waitingForSecondClick) {
                translationStart = point;
                waitingForSecondClick = true;
            } else {
                translationEnd = point;
                waitingForSecondClick = false;
                
                if (selectedShape != null) {
                    double dx = translationEnd.x - translationStart.x;
                    double dy = translationEnd.y - translationStart.y;
                    
                    double[][] matrix = new double[][]{
                        {1, 0, dx},
                        {0, 1, dy},
                        {0, 0, 1}
                    };
                    transform(selectedShape, matrix);
                }
            }
        }

        // 添加处理缩放点击的方法
        private void handleScaleClick(Point point) {
            if (!waitingForFixedPoint) {
                fixedPoint = point;
                waitingForFixedPoint = true;
            } else if (!waitingForEndPoint) {
                // 计算缩放比例
                double distance = point.distance(fixedPoint);
                double baseDistance = 100.0; // 基准距离
                double scale = distance / baseDistance;
                
                if (selectedShape != null) {
                    double[][] matrix = new double[][]{
                        {scale, 0, fixedPoint.x * (1 - scale)},
                        {0, scale, fixedPoint.y * (1 - scale)},
                        {0, 0, 1}
                    };
                    transform(selectedShape, matrix);
                }
                
                // 重置状态
                waitingForFixedPoint = false;
                waitingForEndPoint = false;
                fixedPoint = null;
            }
        }
    }

    public static void main(String[] args) {
        // 确保在 EDT (Event Dispatch Thread) 中运行
        SwingUtilities.invokeLater(() -> {
            ShapeDrawer drawer = new ShapeDrawer();
            drawer.setLocationRelativeTo(null); // 使窗口在屏幕中央显示
            drawer.setVisible(true);
        });
    }
} 