package uistuff;

import java.awt.Color;

/**
 * @author Volker Christian (volker.christian@fh-hagenberg.at)
 * @version 1.0
 */

public class Turtle {
	static int[][][] picture = new int[300][300][3];
	static {
		erase();
		
	}
	
	static double x = 0;
	static double y = 0;
	static int angle = 0;
	static int stepWidth = 1;
	static Color color = Color.BLACK;
	static boolean isDrawing = true;
	
	protected static void putPixel(int x, int y) {
		int yMax = picture.length;
		
		if (yMax - y - 1 < 0 || 
				yMax - y - 1 >= picture.length ||
				x < 0 ||
				x >= picture[0].length ||
				!isDrawing) {
			return;
		}
		
		picture[yMax - y - 1][x][0] = color.getRed();
		picture[yMax - y - 1][x][1] = color.getGreen();
		picture[yMax - y - 1][x][2] = color.getBlue();
	}
	
	
	protected static void drawLine(int x1, int y1, int x2, int y2) {
		int x = x1, y = y1, d = 0, hx = x2 - x1, hy = y2 - y1;
		int xInc = 1;
		int yInc = 1;
		
		if (hx < 0) {
			xInc = -1;
			hx = -hx;
		}
		if (hy < 0) {
			yInc = -1;
			hy = -hy;
		}
		if (hy <= hx) {
			int c = 2 * hx;
			int k = 2 * hy;
			
			while (true) {
				putPixel(x, y);
				if (x == x2) {
					break;
				}
				x += xInc;
				d += k;
				if (d > hx) {
					y += yInc;
					d -= c;
				}
			}
		} else {
			int c = 2 * hy;
			int k = 2 * hx;
			
			while (true) {
				putPixel(x, y);
				if (y == y2) {
					break;
				}
				y += yInc;
				d += k;
				if (d > hy) {
					x += xInc;
					d -= c;
				}
			}
		}
	}
	
	
	public static void forward(double n) {
		double xn = x + n * stepWidth * Math.cos(2.0 * Math.PI * angle / 360.0);
		double yn = y + n * stepWidth * Math.sin(2.0 * Math.PI * angle / 360.0);
		
		drawLine(
				(int) Math.round(x), 
				(int) Math.round(y), 
				(int) Math.round(xn), 
				(int) Math.round(yn));
		x = xn;
		y = yn;
	
		Presenter.updateMovie(picture);
	}
	
	
	public static void setPos(int x, int y) {
		Turtle.x = x;
		Turtle.y = y;
	}
	
	
	public static void setAngle(int angle) {
		Turtle.angle = angle;
	}
	
	
	public static void stepWidth(int width) {
		Turtle.stepWidth = width;
	}
	
	
	public static void left(int angel) {
		Turtle.angle += angel;
	}
	
	
	public static void right(int angel) {
		Turtle.angle -= angel;
	}
	
	
	public static void setColor(Color color) {
		Turtle.color = color;
	}
	
	
	public static void showGraphics(String title) {
		Presenter.viewImage(title, picture);
	}
	
	public static void showMovie(String title) {
		Presenter.viewMovie(title, picture);
	}
	
	
	public static void penUp() {
		Turtle.isDrawing = false;
	}
	
	
	public static void penDown() {
		Turtle.isDrawing = true;
	}
	
	public static void erase() {
		for (int y = 0; y < picture.length; y++) {
			for (int x = 0; x < picture[0].length; x++) {
				picture[y][x][0] = 255;
				picture[y][x][1] = 255;
				picture[y][x][2] = 255;
			}
		}
		
	}
}
