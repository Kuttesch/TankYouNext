package TankYouNext;

import robocode.*;
import robocode.util.Utils;

import java.awt.Color;

public class MissleToe extends AdvancedRobot {

    @Override
    public void run() {
        setAdjustRadarForRobotTurn(true);
        while (true) {
            setTurnRadarRight(360);
            colorize();
            runRandom();
            execute();
        }
    }

    public void runRandom() {
        setAhead(Math.random() * 200);
        setTurnRight(Math.random() * 360);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double bearing = e.getBearingRadians();
        double distance = e.getDistance();
        double enemySpeed = e.getVelocity();
        double enemyHeading = e.getHeadingRadians();

        double absoluteBearing = getHeadingRadians() + bearing;

        double enemyX = getX() + Math.sin(absoluteBearing) * distance;
        double enemyY = getY() + Math.cos(absoluteBearing) * distance;

        System.out.printf("Enemy Position: X=%.2f, Y=%.2f\n", enemyX, enemyY);

        double timeDelta = 10;
        double futureX = enemyX + Math.sin(enemyHeading) * enemySpeed * timeDelta;
        double futureY = enemyY + Math.cos(enemyHeading) * enemySpeed * timeDelta;

        System.out.printf("Predicted Position: X=%.2f, Y=%.2f\n", futureX, futureY);

        aimGunAt(futureX, futureY);
    }

    private void aimGunAt(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angleToTarget = Math.atan2(dx, dy);
        double gunTurn = Utils.normalRelativeAngle(angleToTarget - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);
        fire(1);
    }

    public void colorize() {
        setBodyColor(calculateRandomColor());
        setGunColor(calculateRandomColor());
        setRadarColor(calculateRandomColor());
        setBulletColor(calculateRandomColor());
        setScanColor(calculateRandomColor());
    }

    public Color calculateRandomColor() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);

        return new Color(red, green, blue);
    }


}
