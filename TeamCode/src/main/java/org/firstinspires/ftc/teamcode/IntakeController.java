package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
public class IntakeController extends ComponentControllerBase {
    //public static IntakeController singleton;
    private final DcMotor intake;
    private static final float maxPower = .9f;

    public boolean isIntaking = false;
    public boolean lastFrameInput = false;
    public IntakeController(String intakeMotorName)
    {
        this.intake = opMode.hardwareMap.dcMotor.get(intakeMotorName);
    }

    public static IntakeController createSingleton()
    {
        return new IntakeController("intake");
    }

    @Override
    public void tick()
    {
        float intakePower = -opMode.gamepad1.left_trigger + opMode.gamepad1.right_trigger;
        if (Math.abs(intakePower) >= 0.1) runIntake(intakePower);
        else stopIntake();

        //failed attempt to make intake togglable
        /*if((opMode.gamepad1.left_trigger > 0.25f || opMode.gamepad1.right_trigger > 0.25f) && isIntaking && !lastFrameInput)
        {
            isIntaking = true;
            lastFrameInput = true;
            opMode.telemetry.addLine("yay :)");
        }
        else if((opMode.gamepad1.left_trigger > 0.25f || opMode.gamepad1.right_trigger > 0.25f) && !isIntaking && !lastFrameInput)
        {
            isIntaking = false;
            lastFrameInput = true;
        }
        else if(opMode.gamepad1.left_trigger > 0.25f || opMode.gamepad1.right_trigger > 0.25f)
        {
            lastFrameInput = true;
            opMode.telemetry.addLine("AAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        }
        else lastFrameInput = false;

        if (isIntaking)
        {
            float intakePower = -opMode.gamepad1.left_trigger + opMode.gamepad1.right_trigger;
            runIntake(intakePower);
        }
        else stopIntake();

        opMode.telemetry.addData("left trigger", opMode.gamepad1.left_trigger);
        opMode.telemetry.addData("right trigger", opMode.gamepad1.right_trigger);
        opMode.telemetry.addData("isIntaking", isIntaking);
        opMode.telemetry.addData("lastFrameInput", lastFrameInput);*/
    }

    public void runIntake(float power)
    {
        if(Math.abs(power) >= maxPower) intake.setPower((power >= 0) ? maxPower : -maxPower);
        else intake.setPower(power);
    }

    public void stopIntake()
    {
        intake.setPower(0);
    }
}
