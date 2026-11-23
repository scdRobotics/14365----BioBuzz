package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.pedro.Constants;


@com.qualcomm.robotcore.eventloop.opmode.Autonomous
public class Autonomous extends LinearOpMode
{
    private Follower follower;

    @Override
    public void runOpMode() throws InterruptedException {

        follower = Constants.create(hardwareMap);
        follower.setPose(new Pose(0,0,0));

        TurretController turret= new TurretController("turret", "pollenServo");

        waitForStart();

        turret.setVelocity(900);
        while(turret.getVelocity() <= 900);

    }
}
