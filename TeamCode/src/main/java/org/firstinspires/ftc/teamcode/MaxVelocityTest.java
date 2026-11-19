package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@TeleOp
public class MaxVelocityTest extends LinearOpMode
{
    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx turretMotor = hardwareMap.get(DcMotorEx.class, "turret");
        double maxVelocity = 0;

        waitForStart();

        while(opModeIsActive())
        {
            turretMotor.setPower(1);
            telemetry.addData("velocity", turretMotor.getVelocity());
            maxVelocity = Math.max(maxVelocity, turretMotor.getVelocity());
            telemetry.addData("maxVeloity", maxVelocity);
            telemetry.update();
        }
    }
}
