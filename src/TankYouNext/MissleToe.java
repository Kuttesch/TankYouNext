package TankYouNext;

import java.awt.Color;
import java.util.Random;
import robocode.util.Utils;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class MissleToe extends Robot {

   private EnemyData Enemy;
   private double GunHeading;
   private double RadarHeading;

   public void run() {
      this.setAdjustGunForRobotTurn(true);
      this.setAdjustRadarForGunTurn(true);
      this.setAdjustRadarForRobotTurn(true);

      while(true) {
         this.turnRadarRight(360.0D);
         this.turnGunRight(360.0D);
         int red = (int)(Math.random() * 255.0D);
         int green = (int)(Math.random() * 255.0D);
         int blue = (int)(Math.random() * 255.0D);
         this.setColors(new Color(red, green, blue), new Color(red, green, blue), new Color(red, green, blue));
         this.movement();
      }
   }

   public void movement() {
      Random random = new Random();
      int max_turn = 135;
      int min_turn = 0;
      int randomDegree = random.nextInt(max_turn - min_turn + 1);
      int randomDirection = random.nextInt(2);
      if (randomDirection == 0) {
         this.turnRight((double)randomDegree);
      } else {
         this.turnLeft((double)randomDegree);
      }

      this.ahead(50.0D);
   }

   public void onScannedRobot(ScannedRobotEvent e) {
      double distance = e.getDistance();
      double energy = e.getEnergy();
      double bearing = e.getBearing();
      double velocity = e.getVelocity();

      Enemy = new EnemyData();  // Correct instantiation of the EnemyData class

      Enemy.distance = e.getDistance();
      Enemy.energy = e.getEnergy();
      Enemy.bearing = e.getBearing();
      Enemy.velocity = e.getVelocity();
      Enemy.heading = e.getHeading();
      Enemy.x = this.getX();
      Enemy.y = this.getY();

      Enemy.new_x = Enemy.x + Math.sin(Math.toRadians(Enemy.heading)) * Enemy.distance;
      Enemy.new_y = Enemy.y + Math.cos(Math.toRadians(Enemy.heading)) * Enemy.distance;
      this.gunToXY(Enemy.new_x, Enemy.new_y);

      if (this.getEnergy() > 50.0D) {
         this.fire(3.0D);
      } else {
         this.fire(1.0D);
      }

      if(this.checkIfHit(e, Enemy)) {
         // Fix: Avoid infinite recursion by just scanning again instead of calling onScannedRobot again
         this.scan();
      }
   }

   public boolean checkIfHit(ScannedRobotEvent e, EnemyData Enemy) {
      if ((Enemy.energy - e.getEnergy()) <= 3.0D && (Enemy.energy - e.getEnergy()) >= 0.1D) {
         return true;
      }
      return false;
   }

   public void gunToXY(double x, double y) {
      double dx = x - this.getX();
      double dy = y - this.getY();
      double angleToTarget = Math.atan2(dx, dy);
      double gunTurn = Utils.normalRelativeAngle(angleToTarget - this.getGunHeading());
      if (gunTurn > 0.0D) {
         this.turnGunRight(gunTurn);
      } else {
         this.turnGunLeft(gunTurn);
      }
   }

   // Empty radarLock method can be removed or implemented as necessary
   public void radarLock() {
      // Implement radar lock logic if needed, or remove this method.
   }

   public void onHitByBullet(HitByBulletEvent e) {
      this.back(50.0D);
   }

   public void onHitWall(HitWallEvent e) {
      this.back(50.0D);
      this.turnRight(90.0D);
   }

   public void onHitRobot(HitRobotEvent e) {
      double bearing = e.getBearing();
      double gunTurn = this.getHeading() - this.getGunHeading() + bearing;
      double radarTurn = this.getHeading() - this.getRadarHeading() + bearing;
      this.turnGunRight(gunTurn);
      this.turnRadarRight(radarTurn);
      this.fire(3.0D);
      this.ahead(10.0D);
      this.scan();
   }
   private class EnemyData {
      public double distance;
      public double energy;
      public double bearing;
      public double velocity;
      public double heading;
      public double x;
      public double y;

      public double new_x;
      public double new_y;
}
}


