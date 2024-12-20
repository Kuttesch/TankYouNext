package TankYouNext;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class _new_old_new_old_MissleToe extends AdvancedRobot {
    private HashMap<String, EnemyRobot> enemies = new HashMap<>();
    private EnemyRobot targetEnemy;
    private static final int WALL_BUFFER = 35;

    public void run() {
        setColors(Color.RED, Color.GREEN, Color.BLUE);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            if (getRadarTurnRemaining() == 0) {
                setTurnRadarRight(Double.POSITIVE_INFINITY); // Keep scanning
            }
            moveStrategically(); // Risk-based movement
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        double absBearing = getHeadingRadians() + event.getBearingRadians();
        Point2D.Double enemyPosition = new Point2D.Double(
                getX() + Math.sin(absBearing) * event.getDistance(),
                getY() + Math.cos(absBearing) * event.getDistance()
        );

        EnemyRobot enemy = enemies.getOrDefault(event.getName(), new EnemyRobot());
        enemy.update(event, enemyPosition);
        enemies.put(event.getName(), enemy);

        if (targetEnemy == null || enemy.isHigherPriorityThan(targetEnemy)) {
            targetEnemy = enemy;
        }

        if (targetEnemy != null && targetEnemy.getName().equals(event.getName())) {
            double gunTurn = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians());
            setTurnGunRightRadians(gunTurn);

            if (getGunHeat() == 0) {
                double firePower = Math.min(400 / event.getDistance(), 3);
                setFire(firePower);
            }

            setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians()) * 2);
        }
    }

    public void onRobotDeath(RobotDeathEvent event) {
        if (targetEnemy != null && targetEnemy.getName().equals(event.getName())) {
            targetEnemy = null; // Reset target on death
        }
        enemies.remove(event.getName());
    }

    private void moveStrategically() {
        double x = getX();
        double y = getY();

        if (x < WALL_BUFFER || x > getBattleFieldWidth() - WALL_BUFFER ||
            y < WALL_BUFFER || y > getBattleFieldHeight() - WALL_BUFFER) {
            // Avoid walls
            setTurnRight(90);
            setAhead(100);
        } else if (targetEnemy != null) {
            // Move towards or away from the target based on distance
            double distance = targetEnemy.getDistance();
            if (distance > 200) {
                setAhead(100); // Move closer
            } else {
                setBack(100); // Move away
            }
        }
    }

    private static class EnemyRobot {
        private String name;
        private double energy;
        private double distance;
        private Point2D.Double position;

        public void update(ScannedRobotEvent event, Point2D.Double position) {
            this.name = event.getName();
            this.energy = event.getEnergy();
            this.distance = event.getDistance();
            this.position = position;
        }

        public boolean isHigherPriorityThan(EnemyRobot other) {
            return this.energy < other.energy || this.distance < other.distance;
        }

        public String getName() {
            return name;
        }

        public double getDistance() {
            return distance;
        }
    }
}
