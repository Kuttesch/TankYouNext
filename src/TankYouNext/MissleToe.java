package TankYouNext;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import robocode.*;
import robocode.util.Utils;

public class MissleToe extends AdvancedRobot {

    // Enemy tracking
    private static HashMap<String, EnemyBot> enemies = new HashMap<>();
    private static EnemyBot targetEnemy;

    // Battlefield info
    private static double battlefieldWidth;
    private static double battlefieldHeight;
    private static Point2D.Double battlefieldCenter;

    // Movement control
    private static Point2D.Double currentPosition = new Point2D.Double();
    private static Point2D.Double travelDestination = new Point2D.Double();
    private static final int WALL_BUFFER = 35;

    // Radar and time tracking
    private static long lastRadarTime;
    private static final int RADAR_DELAY = 20;
    private static long time;

    public void run() {
        // Initialization
        this.setColors(Color.CYAN, Color.BLUE, Color.DARK_GRAY, Color.YELLOW, Color.WHITE);
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);
        battlefieldWidth = this.getBattleFieldWidth();
        battlefieldHeight = this.getBattleFieldHeight();
        battlefieldCenter = new Point2D.Double(battlefieldWidth / 2.0, battlefieldHeight / 2.0);

        this.setTurnRadarRight(Double.POSITIVE_INFINITY);

        while (true) {
            time = this.getTime();
            calculateMovement();
            if (this.getRadarTurnRemaining() == 0.0) {
                this.setTurnRadarRight(Double.POSITIVE_INFINITY);
            }
            this.execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        // Track enemy info
        EnemyBot enemy = enemies.get(e.getName());
        if (enemy == null) {
            enemy = new EnemyBot();
            enemies.put(e.getName(), enemy);
        }

        currentPosition.setLocation(this.getX(), this.getY());
        double absoluteBearing = this.getHeadingRadians() + e.getBearingRadians();
        double distance = e.getDistance();

        enemy.update(currentPosition, absoluteBearing, distance, e.getEnergy());

        // Determine target priority
        if (targetEnemy == null || enemy.isHigherPriority(targetEnemy)) {
            targetEnemy = enemy;
        }

        // Targeting and firing
        if (targetEnemy != null) {
            aimAndFire(targetEnemy);
        }
    }

    private void aimAndFire(EnemyBot enemy) {
        double gunTurn = Utils.normalRelativeAngle(Math.atan2(
            enemy.position.getX() - currentPosition.getX(),
            enemy.position.getY() - currentPosition.getY()) - this.getGunHeadingRadians());

        this.setTurnGunRightRadians(gunTurn);

        if (this.getGunHeat() == 0.0 && Math.abs(this.getGunTurnRemaining()) < 10) {
            double firePower = Math.min(3.0, Math.max(0.1, this.getEnergy() / 5));
            this.setFire(firePower);
        }
    }

    private void calculateMovement() {
        double risk;
        double minRisk = Double.MAX_VALUE;

        Point2D.Double bestDestination = null;

        for (double angle = 0; angle < 2 * Math.PI; angle += Math.PI / 8) {
            Point2D.Double candidate = new Point2D.Double(
                currentPosition.getX() + Math.sin(angle) * 100,
                currentPosition.getY() + Math.cos(angle) * 100
            );

            if (!isInsideBattlefield(candidate)) {
                continue;
            }

            risk = calculateRisk(candidate);

            if (risk < minRisk) {
                minRisk = risk;
                bestDestination = candidate;
            }
        }

        if (bestDestination != null) {
            travelDestination.setLocation(bestDestination);
            moveToDestination(bestDestination);
        }
    }

    private void moveToDestination(Point2D.Double destination) {
        double angle = Utils.normalRelativeAngle(Math.atan2(
            destination.getX() - currentPosition.getX(),
            destination.getY() - currentPosition.getY()) - this.getHeadingRadians());

        this.setTurnRightRadians(Math.atan(Math.tan(angle)));
        this.setAhead(currentPosition.distance(destination) * (angle == Math.atan(Math.tan(angle)) ? 1 : -1));
    }

    private boolean isInsideBattlefield(Point2D.Double point) {
        return point.getX() > WALL_BUFFER && point.getX() < battlefieldWidth - WALL_BUFFER
            && point.getY() > WALL_BUFFER && point.getY() < battlefieldHeight - WALL_BUFFER;
    }

    private double calculateRisk(Point2D.Double point) {
        double risk = 0;

        risk += 1.0 / point.distanceSq(battlefieldCenter);

        for (EnemyBot enemy : enemies.values()) {
            risk += 100.0 / point.distanceSq(enemy.position);
        }

        return risk;
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemies.remove(e.getName());
        if (e.getName().equals(targetEnemy)) {
            targetEnemy = null;
        }
    }

    public void onHitWall(HitWallEvent e) {
        this.setBack(50);
        this.setTurnRight(90);
    }

    private static class EnemyBot {
        Point2D.Double position = new Point2D.Double();
        double energy;

        void update(Point2D.Double currentPosition, double absoluteBearing, double distance, double energy) {
            this.position.setLocation(
                currentPosition.getX() + Math.sin(absoluteBearing) * distance,
                currentPosition.getY() + Math.cos(absoluteBearing) * distance
            );
            this.energy = energy;
        }

        boolean isHigherPriority(EnemyBot other) {
            return this.energy < other.energy;
        }
    }
}
