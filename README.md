# 计算机图形学基础绘图程序

这是一个基于Java Swing开发的计算机图形学基础绘图程序集合，包含了多个经典图形学算法的实现。

## 功能模块

### 1. Bresenham直线绘制 (BresenhamLine.java)
- 使用鼠标左键点击画布上的任意位置
- 程序会自动从画布中心到鼠标点击位置绘制一条直线
- 使用Bresenham算法实现，确保线条平滑且像素准确

### 2. Bresenham圆形绘制 (BresenhamCircle.java)
- 画布中心为圆心
- 使用鼠标左键点击确定圆的半径
- 点击位置到圆心的距离即为圆的半径
- 使用Bresenham算法绘制完整的圆形

### 3. Bresenham椭圆绘制 (BresenhamEllipse.java)
- 在顶部输入框中输入长轴(a)和短轴(b)的值
- 点击"绘制"按钮在画布中心绘制椭圆
- 使用改进的Bresenham算法实现椭圆的绘制

### 4. 多边形填充 (PolygonFill.java & EdgeFill.java)
提供了两种填充算法实现：
#### 扫描线填充 (PolygonFill.java)
- 使用鼠标左键依次点击确定多边形顶点
- 使用鼠标右键闭合多边形
- 点击"填充"按钮进行颜色填充
- 可以通过"选择颜色"按钮更改填充颜色
- 使用"清除"按钮重新开始

#### 边缘填充 (EdgeFill.java)
- 操作方式与扫描线填充相同
- 使用更高效的边缘表算法实现填充

### 5. 图形变换工具 (ShapeDrawer.java)
这是一个综合性的图形绘制和变换工具：

#### 绘制功能：
- 可以绘制正方形、正六边形、五角星三种图形
- 在输入框中设定图形大小
- 点击画布确定图形位置

#### 变换功能：
1. 平移变换
   - 选择要变换的图形（鼠标左键点击）
   - 点击"平移"按钮
   - 拖动鼠标进行平移

2. 旋转变换
   - 选择图形后点击"旋转"按钮
   - 右键点击确定旋转中心
   - 拖动鼠标进行旋转

3. 缩放变换
   - 选择图形后点击"缩放"按钮
   - 右键点击确定缩放基准点
   - 拖动鼠标进行缩放

4. 错切变换
   - 选择图形后点击"错切"按钮
   - 右键点击确定错切基准点
   - 拖动鼠标进行x/y方向的错切

## 运行环境要求
- Java Runtime Environment (JRE) 8 或更高版本
- 支持图形界面的操作系统

## 如何运行
1. 确保已安装Java环境
2. 编译所有.java文件
3. 运行需要使用的具体程序，例如：
`````````
bash
javac .java
java ShapeDrawer # 运行图形变换工具
java BresenhamLine # 运行直线绘制工具
`````````
## 运行exe文件
- 可以点击对应的exe文件实现快速部署和运行
## 注意事项
- 所有程序都提供了图形化界面，操作直观
- 建议使用鼠标进行操作
- 如遇到异常，可以使用"清除"按钮重置画布
