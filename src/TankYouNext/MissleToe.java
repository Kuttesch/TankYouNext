package TankYouNext;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.ScannedRobotEvent;

public class MissleToe extends AdvancedRobot {
   boolean movingForward;

   public void run() {
      this.setAdjustRadarForRobotTurn(true);
      this.setAdjustGunForRobotTurn(true);
      this.setAhead(10000.0D);
      this.movingForward = true;

      while(true) {
         this.turnRadarRightRadians(Double.POSITIVE_INFINITY);
      }
   }

   public void onScannedRobot(ScannedRobotEvent e) {
      double enemyBearing = this.getHeading() + e.getBearing();
      double enemyX = this.getX() + e.getDistance() * Math.sin(Math.toRadians(enemyBearing));
      double enemyY = this.getY() + e.getDistance() * Math.cos(Math.toRadians(enemyBearing));
      double dx = enemyX - this.getX();
      double dy = enemyY - this.getY();
      double theta = Math.toDegrees(Math.atan2(dx, dy));
      this.turnRadarRight(this.normalizeBearing(this.getHeading() - (this.getRadarHeading() + e.getBearing())));
      double gunTurnAmt = this.normalizeBearing(theta - this.getGunHeading());
      this.setTurnGunRight(gunTurnAmt);
      if (Math.abs(gunTurnAmt) <= 3.0D) {
         this.setFire(Math.min(500.0D / e.getDistance(), 3.0D));
      }

      if (e.getDistance() > 150.0D) {
         if (this.movingForward) {
            this.setTurnRight(this.normalizeBearing(enemyBearing + 80.0D));
         } else {
            this.setTurnRight(this.normalizeBearing(enemyBearing + 100.0D));
         }
      } else if (this.movingForward) {
         this.setBack(40.0D);
         this.movingForward = false;
      } else {
         this.setAhead(40.0D);
         this.movingForward = true;
      }

   }

   public void onHitByBullet(HitByBulletEvent e) {
      if (this.movingForward) {
         this.setBack(10000.0D);
         this.movingForward = false;
      } else {
         this.setAhead(10000.0D);
         this.movingForward = true;
      }

   }

   double normalizeBearing(double angle) {
      while(angle > 180.0D) {
         angle -= 360.0D;
      }

      while(angle < -180.0D) {
         angle += 360.0D;
      }

      return angle;
   }
}