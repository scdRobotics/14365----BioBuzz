package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
public class Localization {
     private final Follower follower;
     public Localization(Follower follower){
         this.follower = follower;
     }
     public double getX(){
         return follower.pose().x();
     }
    public double getY(){
        return follower.pose().y();
    }
    public double getHeading(){
        return follower.pose().heading();
    }
    public double getHeadDegrees(){
        return Math.toDegrees(follower.pose().heading());
    }
    public Pose getPose(){
         return follower.pose();
    }
    public double distanceto(double targetX,double targetY){
         double dx = targetX - getX();
         double dy = targetY - getY();
         return Math.hypot(dx,dy);
    }
    public Boolean isNear(double targetX, double targetY, double tolerance){
         return distanceto(targetX, targetY) <= tolerance;
    }
}
