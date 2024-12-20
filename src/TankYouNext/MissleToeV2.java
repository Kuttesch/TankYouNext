package TankYouNext;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

public class MissleToeV2 extends AdvancedRobot {
    
    // Store the previous positions of the enemy for predictive targeting
    private ArrayList<Point2D.Double> enemyPositions = new ArrayList<>();
    // Store velocity and energy data for advanced analysis
    private HashMap<String, Double> enemyVelocityMap = new HashMap<>();
    private double lastEnergy = 100;
    
    // Movement-related variables
    private boolean isDodging = false;
    private static final double WALL_STICK_DISTANCE = 100; // Stick to the wall at this distance
    
    @Override
    public void run() {
        setColors(Color.BLUE, Color.BLACK, Color.CYAN); // Set bot colors
        setScanColor(Color.YELLOW);
        
        // Main robot loop
        while (true) {
            // Movement: Adaptive strategy (Wave Surfing and Wall-Sticking)
            if (!isDodging) {
                moveWallStick();
            }
            // Radar: Continuous scanning with locking strategy
            setRadarTurnRight(360);
            // Shooting: Predictive targeting with Bullet Tracking and Energy Management
            shootAtTarget();
            // Reset dodge flag after some time
            isDodging = false;
        }
    }

    // Wall-sticking movement pattern
    private void moveWallStick() {
        // Get the closest wall and move towards it to stick
        double angleToWall = getWallAngle();
        double distanceToWall = getDistanceToWall();
        
        // If we are too far from the wall, move toward it
        if (distanceToWall > WALL_STICK_DISTANCE) {
            setTurnRight(angleToWall - getHeading());
            setAhead(distanceToWall - WALL_STICK_DISTANCE); // Move closer to the wall
        } 
        // If we're too close to the wall, adjust
        else if (distanceToWall < WALL_STICK_DISTANCE / 2) {
            setTurnRight(angleToWall + 180 - getHeading());
            setAhead(WALL_STICK_DISTANCE - distanceToWall); // Move away from the wall
        } 
        // Randomize small movements while sticking to the wall
        else {
            setTurnRight(Utils.normalRelativeAngleDegrees(Math.random() * 360 - getHeading()));
            setAhead(50 + Math.random() * 100); // Small random movements
        }
    }

    // Calculate the angle to the closest wall
    private double getWallAngle() {
        double angleToWall = 0;
        if (getX() < WALL_STICK_DISTANCE) {
            angleToWall = 90; // Wall on the left
        } else if (getX() > getBattleFieldWidth() - WALL_STICK_DISTANCE) {
            angleToWall = 270; // Wall on the right
        } else if (getY() < WALL_STICK_DISTANCE) {
            angleToWall = 0; // Wall above
        } else if (getY() > getBattleFieldHeight() - WALL_STICK_DISTANCE) {
            angleToWall = 180; // Wall below
        }
        return angleToWall;
    }

    // Get the distance to the nearest wall
    private double getDistanceToWall() {
        double minX = Math.min(getX(), getBattleFieldWidth() - getX());
        double minY = Math.min(getY(), getBattleFieldHeight() - getY());
        return Math.min(minX, minY);
    }

    // Check if there's a bullet in the proximity
    private boolean isBulletInRange() {
        for (ScannedRobotEvent e : getScannedRobots()) {
            double bulletDistance = e.getDistance();
            if (bulletDistance < 400) { // Bullet is within range
                return true;
            }
        }
        return false;
    }

    // Perform wave surfing to dodge incoming bullets
    private void performWaveSurfing() {
        // Get the most recent bullet data and dodge
        for (ScannedRobotEvent e : getScannedRobots()) {
            double bulletDistance = e.getDistance();
            double bulletVelocity = 20 - 3 * e.getEnergy();
            double timeToImpact = bulletDistance / bulletVelocity;
            
            double predictedX = getX() + Math.sin(getHeadingRadians()) * e.getVelocity() * timeToImpact;
            double predictedY = getY() + Math.cos(getHeadingRadians()) * e.getVelocity() * timeToImpact;

            double angleToDodge = Utils.normalRelativeAngle(Math.atan2(predictedX - getX(), predictedY - getY()));
            setTurnRight(angleToDodge);
            setAhead(200); // Move away from the bullet trajectory
        }
    }

    // Normalize angle to ensure turning within -180 to 180 degrees
    private double normalizeAngle(double angle) {
        return Utils.normalRelativeAngleDegrees(angle);
    }

    // Predictive targeting with Bullet Tracking
    private void shootAtTarget() {
        // Get the closest enemy
        ScannedRobotEvent enemy = getClosestEnemy();
        if (enemy != null) {
            // Predict the position of the enemy using their movement
            double predictedX = enemy.getX() + enemy.getVelocity() * Math.cos(enemy.getHeadingRadians());
            double predictedY = enemy.getY() + enemy.getVelocity() * Math.sin(enemy.getHeadingRadians());
            
            // Calculate the angle to the predicted position
            double angleToTarget = Math.atan2(predictedX - getX(), predictedY - getY());
            double radarTurn = Utils.normalRelativeAngleDegrees(angleToTarget - getRadarHeading());
            setRadarTurnRight(radarTurn);
            
            // Fire based on the calculated position and energy levels
            if (getEnergy() > 20) {
                fire(2); // Fire at moderate power if energy is sufficient
            }
        }
    }

    // Get the closest enemy (simple heuristic)
    private ScannedRobotEvent getClosestEnemy() {
        double closestDistance = Double.MAX_VALUE;
        ScannedRobotEvent closestEnemy = null;
        for (ScannedRobotEvent event : getScannedRobots()) {
            double distance = event.getDistance();
            if (distance < closestDistance) {
                closestDistance = distance;
                closestEnemy = event;
            }
        }
        return closestEnemy;
    }

    // Track enemy positions and velocities for advanced targeting
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Save enemy position for future prediction
        enemyPositions.add(new Point2D.Double(e.getX(), e.getY()));
        
        // Store enemy velocity for prediction purposes
        enemyVelocityMap.put(e.getName(), e.getVelocity());
        
        // Energy consumption analysis
        double energyUsed = lastEnergy - e.getEnergy();
        if (energyUsed > 0) {
            // The robot fired, so we manage the energy accordingly
            lastEnergy = e.getEnergy();
        }
    }

    // Handle when we get hit by a bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Move away from the bullet hit location (avoid further damage)
        double moveAwayAngle = normalizeAngle(e.getHeading() + 180);
        setTurnRight(moveAwayAngle);
        setAhead(150); // Move away from danger zone
    }

    // Handle robot death and cleanup
    @Override
    public void onDeath(DeathEvent e) {
        System.out.println("My robot has been defeated!");
    }
}
