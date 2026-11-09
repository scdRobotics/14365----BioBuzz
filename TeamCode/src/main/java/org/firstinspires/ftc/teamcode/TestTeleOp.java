package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TestTeleOp extends LinearOpMode {
    @Override
    public void runOpMode()
    {
        ComponentControllerBase.setOpMode(this);

        Drivetrain.createSingleton();
        IntakeController.createSingleton();
        TurretController.createSingleton();

        ComponentControllerBase.initControllers();

        waitForStart();

        while(opModeIsActive())
        {
            ComponentControllerBase.tickControllers();
        }
    }
}
