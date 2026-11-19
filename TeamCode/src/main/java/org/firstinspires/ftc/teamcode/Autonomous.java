package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous
public class Autonomous extends LinearOpMode
{
    @Override
    public void runOpMode() throws InterruptedException {
        TurretController turret= new TurretController("turret", "pollenServo");

        waitForStart();

        turret.setVelocity(900);
        while(turret.getVelocity() <= 900);

    }
}
