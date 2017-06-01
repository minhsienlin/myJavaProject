package maincontrol;

import uistuff.Turtle;

public class Staircase {
	public static void main(String[] args) {
		Turtle.setPos(10, 10);
		for (int i = 0; i < 14; i++) {
			Turtle.forward(20);
			Turtle.left(90);
			Turtle.forward(20);
			Turtle.right(90);
		}
		Turtle.showGraphics("Staircase");
	}
}