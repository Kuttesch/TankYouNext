package TankYouNext;

import robocode.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.Map;
import robocode.util.Utils;

public class MissleToe extends AdvancedRobot {
    private static final double WALL_MARGIN = 18; 
    private static final double MAX_BULLET_POWER = 3.0;
    private static final double MIN_BULLET_POWER = 1.0;
    private static final double CIRCLE_RADIUS = 150;
    private static final double CHANGE_DIRECTION_PROBABILITY = 0.02;
    private static final int FIRE_DISTANCE = 400;
    
    private double moveDirection = 1;
    private Map<String, EnemyBot> enemies = new HashMap<>();
    private EnemyBot target;

    public void run() {
        setColors(Color.RED, Color.BLACK, Color.WHITE); // Set robot colors
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        while (true) {
            scanForEnemies();
            moveBot();
            aimAndFire();
            execute();
        }
    }

    private void scanForEnemies() {
        if (getRadarTurnRemaining() == 0) {
            setTurnRadarRight(Double.POSITIVE_INFINITY);
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        String enemyName = e.getName();
        double distance = e.getDistance();
        
        if (!enemies.containsKey(enemyName)) {
            enemies.put(enemyName, new EnemyBot());
        }
        
        EnemyBot enemy = enemies.get(enemyName);
        enemy.update(e, this);
        
        if (target == null || distance < target.getDistance()) {
            target = enemy;
        }
        
        double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
        double radarTurn = Utils.normalRelativeAngle(angleToEnemy - getRadarHeadingRadians());
        setTurnRadarRightRadians(2 * radarTurn);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        moveDirection = -moveDirection; // Change movement direction on hit
    }

    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection; // Change movement direction on wall hit
    }

    private void moveBot() {
        double x = getX();
        double y = getY();
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();

        if (x < WALL_MARGIN || x > width - WALL_MARGIN || y < WALL_MARGIN || y > height - WALL_MARGIN) {
            moveDirection = -moveDirection;
        }

        if (Math.random() < CHANGE_DIRECTION_PROBABILITY) {
            moveDirection = -moveDirection;
        }

        setAhead(moveDirection * 100);
        setTurnRightRadians(Math.sin(getTime() / 20.0));
    }

    private void aimAndFire() {
        if (target == null) {
            return;
        }

        double absoluteBearing = getHeadingRadians() + target.getBearingRadians();
        double enemyVelocity = target.getVelocity();
        double bulletPower = Math.min(MAX_BULLET_POWER, Math.max(MIN_BULLET_POWER, FIRE_DISTANCE / target.getDistance()));
        
        double predictedX = target.predictX(this);
        double predictedY = target.predictY(this);
        double angleToEnemy = Math.atan2(predictedX - getX(), predictedY - getY());
        double gunTurn = Utils.normalRelativeAngle(angleToEnemy - getGunHeadingRadians());
        
        setTurnGunRightRadians(gunTurn);
        
        if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < 10) {
            setFire(bulletPower);
        }
    }

    public void onRobotDeath(RobotDeathEvent e) {
        if (e.getName().equals(target.getName())) {
            target = null; // Choose a new target if the current one dies
        }
    }

    static class EnemyBot {
        private String name;
        private double x, y;
        private double bearingRadians;
        private double distance;
        private double headingRadians;
        private double velocity;
        private long lastSeen;

        public void update(ScannedRobotEvent e, AdvancedRobot bot) {
            this.name = e.getName();
            this.bearingRadians = e.getBearingRadians();
            this.distance = e.getDistance();
            this.headingRadians = e.getHeadingRadians();
            this.velocity = e.getVelocity();
            this.lastSeen = bot.getTime();
            
            double angle = bot.getHeadingRadians() + e.getBearingRadians();
            this.x = bot.getX() + Math.sin(angle) * e.getDistance();
            this.y = bot.getY() + Math.cos(angle) * e.getDistance();
        }

        public double getBearingRadians() {
            return bearingRadians;
        }

        public double getDistance() {
            return distance;
        }

        public double getVelocity() {
            return velocity;
        }

        public String getName() {
            return name;
        }

        public double predictX(AdvancedRobot bot) {
            double time = (bot.getTime() - lastSeen);
            return x + Math.sin(headingRadians) * velocity * time;
        }

        public double predictY(AdvancedRobot bot) {
            double time = (bot.getTime() - lastSeen);
            return y + Math.cos(headingRadians) * velocity * time;
        }
    }
}