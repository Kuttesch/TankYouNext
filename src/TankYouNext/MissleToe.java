package TankYouNext;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class MissleToe extends Robot {
   private Map<String, Double> enemies = new HashMap<>(); // Store enemy name and risk score
   private String target = null; // Current target robot

   public void run() {
      this.setAdjustGunForRobotTurn(true);
      this.setAdjustRadarForGunTurn(true);
      this.setAdjustRadarForRobotTurn(true);

      while (true) {
         this.turnRadarRight(360.0D); // Scan the battlefield
         avoidWalls(); // Check and avoid walls
         target = getHighestRiskTarget(); // Determine the current highest-risk target
         if (target != null) {
            trackAndAttackTarget();
         } else {
            this.movement(); // Move randomly if no valid target is found
         }
      }
   }

   private void trackAndAttackTarget() {
      // Get the bearing of the target and adjust gun/radar to aim
      double targetBearing = enemies.getOrDefault(target, 0.0);
      double gunTurn = normalizeAngle(this.getHeading() - this.getGunHeading() + targetBearing);
      double radarTurn = normalizeAngle(this.getHeading() - this.getRadarHeading() + targetBearing);
      this.turnGunRight(gunTurn);
      this.turnRadarRight(radarTurn);
      this.fire(3.0); // Fire at full power
   }

   private String getHighestRiskTarget() {
      // Find the robot with the highest risk score
      String highestRiskTarget = null;
      double highestRiskScore = Double.MIN_VALUE;

      for (Map.Entry<String, Double> entry : enemies.entrySet()) {
         if (entry.getValue() > highestRiskScore) {
            highestRiskTarget = entry.getKey();
            highestRiskScore = entry.getValue();
         }
      }
      return highestRiskTarget;
   }

   private double calculateRisk(double distance, double energy) {
      // Simple risk calculation: prioritize closer and lower-energy targets
      return (1000 - distance) + (100 - energy);
   }

   public void onScannedRobot(ScannedRobotEvent e) {
      // Update or add the scanned robot's risk score
      double risk = calculateRisk(e.getDistance(), e.getEnergy());
      enemies.put(e.getName(), risk);

      // If the scanned robot is too close, evade
      if (e.getDistance() < 50) {
         this.back(100.0);
         this.turnRight(90.0);
      }

      // If the scanned robot is our current target, adjust actions
      if (target != null && target.equals(e.getName())) {
         double bearing = e.getBearing();
         double gunTurn = normalizeAngle(this.getHeading() - this.getGunHeading() + bearing);
         double radarTurn = normalizeAngle(this.getHeading() - this.getRadarHeading() + bearing);
         this.turnGunRight(gunTurn);
         this.turnRadarRight(radarTurn);
         this.fire(3.0);
      }
   }

   public void onHitByBullet(HitByBulletEvent e) {
      this.back(50.0); // Move back to evade
   }

   public void onHitWall(HitWallEvent e) {
      this.back(50.0); // Back off from the wall
      this.turnRight(90.0); // Turn away from the wall
   }

   public void onHitRobot(HitRobotEvent e) {
      this.back(100.0); // Back off immediately
      this.turnRight(90.0); // Turn away from the robot
   }

   private void avoidWalls() {
      // Check the robot's position and adjust to avoid hitting walls
      double battlefieldWidth = getBattleFieldWidth();
      double battlefieldHeight = getBattleFieldHeight();
      double x = getX();
      double y = getY();
      double margin = 50.0; // Distance to maintain from walls

      if (x < margin || x > battlefieldWidth - margin || y < margin || y > battlefieldHeight - margin) {
         this.turnRight(90.0); // Turn away from the wall
         this.back(50.0); // Move back to avoid collision
      }
   }

   private void movement() {
      Random random = new Random();
      int randomTurn = random.nextInt(90) - 45; // Turn randomly between -45 and 45 degrees
      this.turnRight(randomTurn);
      this.ahead(50.0); // Move forward
   }

   private double normalizeAngle(double angle) {
      // Normalize an angle to be within -180 to 180 degrees
      while (angle > 180) angle -= 360;
      while (angle < -180) angle += 360;
      return angle;
   }
}
