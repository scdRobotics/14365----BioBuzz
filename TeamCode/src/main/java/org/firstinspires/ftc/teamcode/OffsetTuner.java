package org.firstinspires.ftc.teamcode;

import com.pedropathing.tuning.autotune.Tuner;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@TeleOp
public class OffsetTuner extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException
    {
        ComponentControllerBase.setOpMode(this);
        ComponentControllerBase.Controllers.clear();

        double xOffset=-7.375, yOffset=2.75;
        Odometry.createSingleton();
        Drivetrain.createSingleton();
        ComponentControllerBase.Controllers.add(Odometry.singleton);
        ComponentControllerBase.Controllers.add(Drivetrain.singleton);

        waitForStart();
        while(opModeIsActive())
        {
            telemetry.addData("xOffset", xOffset);
            telemetry.addData("yOffset", yOffset);

            telemetry.addData("y pod encoder", Odometry.singleton.odo.getEncoderY());
            ComponentControllerBase.tickControllers();
            Odometry.singleton.setOffsets(xOffset, yOffset);

            if(gamepad1.a)
            {
                Odometry.singleton.resetPosAndIMU();
            }

            if(gamepad1.dpadUpWasPressed())
            {
                xOffset+=0.1;
            }
            if(gamepad1.dpadDownWasPressed())
            {
                xOffset-=0.1;
            }

            if(gamepad1.dpadRightWasPressed())
            {
                yOffset+= 0.1;
            }

            if(gamepad1.dpadLeftWasPressed())
            {
                yOffset-=0.1;
            }
        }
    }
}
