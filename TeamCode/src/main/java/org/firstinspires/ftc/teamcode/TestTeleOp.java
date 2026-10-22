package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TestTeleOp extends LinearOpMode {
    @Override
    public void runOpMode()
    {
        Drivetrain.createSingleton();
        IntakeController.createSingleton();

        ComponentControllerBase.initControllers(this);

        waitForStart();

        while(opModeIsActive())
        {
            ComponentControllerBase.tickControllers();
        }
    }
}
