package TankYouNext;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.util.Random;

public class MissleToe extends AdvancedRobot {

    boolean movingForward;
    boolean radarLocked = false;

    @Override
    public void run() {
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        movingForward = true;

        while (true) {
            setTurnRadarRight(360); // Keep scanning the radar
            colorize(); // Randomize colors
            runSmartMovement(); // Use smarter movement
            execute(); // Perform actions

            // Call to the random movement and fire strategy
            if (movingForward) {
                ahead(100);
            } else {
                back(100);
            }
            movingForward = !movingForward; // Alternate movement direction

            // Wall avoidance logic: Check if we are too far from the walls and adjust position.
            avoidWalls();

            // Keep radar locked if an enemy is near
            if (radarLocked) {
                setTurnRadarRight(360); // Keep radar locked
            }
        }
    }

    // Perform random yet smarter movement
    public void runSmartMovement() {
        double x = getX();
        double y = getY();
        double fieldWidth = getBattleFieldWidth();
        double fieldHeight = getBattleFieldHeight();

        // Avoid corners by slightly moving towards the center if we are in the edges
        if (x < 100 || x > fieldWidth - 100 || y < 100 || y > fieldHeight - 100) {
            double turnDirection = Math.random() * 360;
            setTurnRight(turnDirection); 
            setAhead(Math.random() * 150);
        } else {
            setAhead(Math.random() * 100);
            setTurnRight(Math.random() * 180);
        }
    }

    // Targeting the predicted position of the enemy (from MissleToe)
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double bearing = e.getBearingRadians();
        double distance = e.getDistance();
        double enemySpeed = e.getVelocity();
        double enemyHeading = e.getHeadingRadians();

        double absoluteBearing = getHeadingRadians() + bearing;

        // Calculate enemy's current position
        double enemyX = getX() + Math.sin(absoluteBearing) * distance;
        double enemyY = getY() + Math.cos(absoluteBearing) * distance;

        // Predict future position of the enemy
        double timeDelta = 10;
        double futureX = enemyX + Math.sin(enemyHeading) * enemySpeed * timeDelta;
        double futureY = enemyY + Math.cos(enemyHeading) * enemySpeed * timeDelta;

        aimGunAt(futureX, futureY); // Aim at the predicted position

        // Fire based on distance with adaptive firepower
        double firePower = calculateAdaptiveFirePower(distance);
        fire(firePower); // Adjust firepower based on enemy distance

        // Lock the radar when an enemy is detected
        radarLocked = true;
    }

    // Adaptive firepower calculation based on distance
    private double calculateAdaptiveFirePower(double distance) {
        if (distance < 100) {
            return 3.0; // Fire at max power if close
        } else if (distance < 300) {
            return 2.0; // Medium power at mid-range
        } else {
            return 1.0; // Lower power at long range
        }
    }

    // Aim the gun at the calculated future position (from MissleToe)
    private void aimGunAt(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        double angleToTarget = Math.atan2(dx, dy);
        double gunTurn = Utils.normalRelativeAngle(angleToTarget - getGunHeadingRadians());
        setTurnGunRightRadians(gunTurn);
    }

    // Randomize robot's colors (from MissleToe)
    public void colorize() {
        setBodyColor(calculateRandomColor());
        setGunColor(calculateRandomColor());
        setRadarColor(calculateRandomColor());
        setBulletColor(calculateRandomColor());
        setScanColor(calculateRandomColor());
    }

    // Generate a random color (from MissleToe)
    public Color calculateRandomColor() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        return new Color(red, green, blue);
    }

    // Handle hits by bullets (from Leopard4 and Leopard3)
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        back(50); // Move away after being hit
        dodgeBullet(); // Implement dodge if necessary
    }

    // Handle wall collisions (from Leopard3)
    @Override
    public void onHitWall(HitWallEvent e) {
        back(50);
        turnRight(90);
    }

    // Handle robot collisions (from Leopard3)
    @Override
    public void onHitRobot(HitRobotEvent e) {
        double bearing = e.getBearing();
        double gunTurn = getHeading() - getGunHeading() + bearing;
        double radarTurn = getHeading() - getRadarHeading() + bearing;
        turnGunRight(gunTurn);
        turnRadarRight(radarTurn);
        fire(3);
        ahead(10);
        scan();
    }

    // Bullet-dodging logic
    private void dodgeBullet() {
        // Predict the bullet’s trajectory and move accordingly
        double bulletHeading = getGunHeadingRadians(); // Assume the bullet's direction is from the gun's heading
        double dodgeDirection = bulletHeading + Math.PI / 2; // 90 degrees away from bullet

        // Move in a direction to dodge
        setTurnRightRadians(Utils.normalRelativeAngle(dodgeDirection - getHeadingRadians()));
        setAhead(150); // Move away quickly
    }

    // Keep robot near the walls
    private void avoidWalls() {
        double wallMargin = 100; // Distance from the wall where we want to stay
        if (getX() < wallMargin) {
            setTurnRight(90); // Turn toward the wall
            setAhead(50); // Move toward the wall
        } else if (getX() > getBattleFieldWidth() - wallMargin) {
            setTurnLeft(90); // Turn toward the wall
            setAhead(50); // Move toward the wall
        }
        if (getY() < wallMargin) {
            setTurnRight(90); // Turn toward the wall
            setAhead(50); // Move toward the wall
        } else if (getY() > getBattleFieldHeight() - wallMargin) {
            setTurnLeft(90); // Turn toward the wall
            setAhead(50); // Move toward the wall
        }
    }
}
