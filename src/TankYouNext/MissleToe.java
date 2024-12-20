package TankYouNext;

import robocode.*;
import java.awt.Color;

public class MissleToe extends AdvancedRobot {
    @Override
    public void run() {
        while (true) {
            colorize();
        }
    }

    public void colorize() {
        setBodyColor(calculateRandomColor());
    }

    public Color calculateRandomColor() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);

        return new Color(red, green, blue);
    }
}
