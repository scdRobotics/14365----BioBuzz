package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp
public class TestTeleOp extends LinearOpMode {
    @Override
    public void runOpMode()
    {
        ComponentControllerBase.setOpMode(this);
        ComponentControllerBase.Controllers.clear();

        Drivetrain.createSingleton();
        IntakeController.createSingleton();
        TurretController.createSingleton();

        ComponentControllerBase.Controllers.add(Drivetrain.singleton);
        ComponentControllerBase.Controllers.add(IntakeController.singleton);
        ComponentControllerBase.Controllers.add(TurretController.singleton);
        ComponentControllerBase.initControllers();

        waitForStart();

        while(opModeIsActive())
        {
            ComponentControllerBase.tickControllers();
        }
    }
}
