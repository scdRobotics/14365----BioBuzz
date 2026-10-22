package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
public class IntakeController extends ComponentControllerBase {
    public static IntakeController singleton;
    private final DcMotor intake;
    public IntakeController(String intakeMotorName)
    {
        this.intake = opMode.hardwareMap.dcMotor.get(intakeMotorName);
    }

    public static void createSingleton()
    {
        singleton = new IntakeController("intake");
    }

    @Override
    public void tick()
    {
        float intakePower = -opMode.gamepad1.left_trigger + opMode.gamepad1.right_trigger;
        if (Math.abs(intakePower) >= 0.1)
        {
            runIntake(intakePower);
        }
        else stopIntake();
    }

    public void runIntake(float power)
    {
        intake.setPower(power);
    }

    public void stopIntake()
    {
        intake.setPower(0);
    }
}
