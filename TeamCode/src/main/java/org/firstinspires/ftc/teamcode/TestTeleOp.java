package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TestTeleOp extends LinearOpMode {
    IntakeController intakeController;
    TurretController turretController;
    Drivetrain drivetrain;

    @Override
    public void runOpMode()
    {
        ComponentControllerBase.initControllers(this);

        drivetrain = Drivetrain.createSingleton();
        intakeController = IntakeController.createSingleton();
        turretController = TurretController.createSingleton();

        waitForStart();

        while(opModeIsActive())
        {
            ComponentControllerBase.tickControllers();
            drivetrain.tick();
            intakeController.tick();
            turretController.tick();
        }
    }
}
