package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
public class IntakeController extends ComponentControllerBase {
    public static IntakeController singleton;
    private final DcMotor intake;
    private static final float maxPower = 0.6f;
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
            if(intakePower < 0)
            {
                intake.setPower(Math.max(intakePower, -maxPower));
            }
            else {
                intake.setPower(Math.min(intakePower, maxPower));
            }
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
