package TankYouNext;

import java.awt.Color;
import java.util.Random;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class MissleToe extends Robot {
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
      double time = distance / (20.0D - 3.0D * energy);
      double gunTurn = this.getHeading() - this.getGunHeading() + bearing;
      double radarTurn = this.getHeading() - this.getRadarHeading() + bearing;
      this.turnGunRight(gunTurn);
      this.turnRadarRight(radarTurn);
      this.fire(3.0D);
      this.ahead(velocity * time);
      this.scan();
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
}