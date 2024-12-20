package TankYouNext;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.Rules;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class MissleMage extends AdvancedRobot {
    private static HashMap<String, EnemyBot> enemies = new HashMap<>();
    private static EnemyBot targetEnemy;
    private static double oldEnemyHeading;
    private static Point2D.Double travelDestination = new Point2D.Double();
    private static Point2D.Double currentPosition = new Point2D.Double();
    private static double battlefieldWidth;
    private static double battlefieldHeight;
    private Random random = new Random();

    public void run() {
        this.setColors(new Color(48, 32, 91), new Color(107, 66, 119), new Color(107, 66, 119), new Color(225, 248, 250), new Color(121, 199, 243));
        this.setAdjustGunForRobotTurn(true);
        this.setAdjustRadarForGunTurn(true);
        battlefieldWidth = this.getBattleFieldWidth();
        battlefieldHeight = this.getBattleFieldHeight();

        while (true) {
            currentPosition.setLocation(this.getX(), this.getY());
            if (this.getRadarTurnRemaining() == 0.0D) {
                this.setTurnRadarRight(Double.POSITIVE_INFINITY);
            }

            this.moveStrategically();
            this.execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        EnemyBot enemy = enemies.get(e.getName());
        if (enemy == null) {
            enemies.put(e.getName(), enemy = new EnemyBot());
        }

        double absoluteBearing = this.getHeadingRadians() + e.getBearingRadians();
        enemy.updatePosition(currentPosition.getX() + Math.sin(absoluteBearing) * e.getDistance(),
                currentPosition.getY() + Math.cos(absoluteBearing) * e.getDistance());
        enemy.energy = e.getEnergy();
        enemy.distance = e.getDistance();
        enemy.attraction = e.getEnergy() < 20.0D ? e.getDistance() * 0.75D : e.getDistance();

        if (targetEnemy == null || enemy.attraction < targetEnemy.attraction) {
            targetEnemy = enemy;
        }

        if (targetEnemy == enemy) {
            double bulletPower = Math.min(3.0D, Math.max(0.1D, (20.0 - e.getDistance()) / 10.0D));
            double gunTurn = Utils.normalRelativeAngle(absoluteBearing - this.getGunHeadingRadians());
            this.setTurnGunRightRadians(gunTurn);

            if (this.getGunHeat() == 0.0D && Math.abs(this.getGunTurnRemainingRadians()) < 0.1D) {
                this.setFire(bulletPower);
            }

            double radarTurn = Utils.normalRelativeAngle(absoluteBearing - this.getRadarHeadingRadians()) * 2;
            this.setTurnRadarRightRadians(radarTurn);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        enemies.remove(e.getName());
        if (targetEnemy != null && targetEnemy.name.equals(e.getName())) {
            targetEnemy = null;
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        this.setBack(50);
        this.setTurnRight(random.nextInt(90));
    }

    public void onHitWall(HitWallEvent e) {
        double bearing = Utils.normalRelativeAngle(this.getHeadingRadians() - e.getBearingRadians());
        this.setBack(50);
        this.setTurnRightRadians(bearing);
    }

    public void onHitRobot(HitRobotEvent e) {
        double bearing = this.getHeadingRadians() + e.getBearingRadians();
        this.setBack(20);
        this.setTurnRightRadians(Utils.normalRelativeAngle(bearing));
    }

    private void moveStrategically() {
        if (targetEnemy != null) {
            double distance = Math.max(100, targetEnemy.distance * 0.5);
            double angle = Utils.normalRelativeAngle(Math.atan2(currentPosition.getX() - targetEnemy.x,
                    currentPosition.getY() - targetEnemy.y) - this.getHeadingRadians());
            this.setTurnRightRadians(angle);
            this.setAhead(distance);
        } else {
            this.randomMovement();
        }
    }

    private void randomMovement() {
        int turnAngle = random.nextInt(135);
        if (random.nextBoolean()) {
            this.setTurnRight(turnAngle);
        } else {
            this.setTurnLeft(turnAngle);
        }

        this.setAhead(50);
    }

    public void onWin(WinEvent e) {
        this.setTurnRight(Double.POSITIVE_INFINITY);
        this.setTurnGunRight(Double.NEGATIVE_INFINITY);
        this.setScanColor(new Color(121, 199, 243));
    }

    private static class EnemyBot {
        String name;
        double x;
        double y;
        double energy;
        double distance;
        double attraction;

        void updatePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
