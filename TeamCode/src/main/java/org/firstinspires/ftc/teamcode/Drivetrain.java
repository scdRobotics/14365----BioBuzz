package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Drivetrain extends ComponentControllerBase{
    public static Drivetrain singleton;
    private final DcMotor frontLeft;
    private final DcMotor backLeft;
    private final DcMotor frontRight;
    private final DcMotor backRight;

    private Drivetrain(String frontLeft, String frontRight, String backLeft, String backRight)
    {
        this.frontLeft = opMode.hardwareMap.dcMotor.get(frontLeft);
        this.frontRight = opMode.hardwareMap.dcMotor.get(frontRight);
        this.backLeft = opMode.hardwareMap.dcMotor.get(backLeft);
        this.backRight = opMode.hardwareMap.dcMotor.get(backRight);

        this.frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        this.backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public static void createSingleton()
    {
        singleton = new Drivetrain("frontLeft", "frontRight", "backLeft", "backRight");
    }

    @Override
    public void tick()
    {
        double x = -opMode.gamepad1.left_stick_x;
        double y = opMode.gamepad1.left_stick_y;
        double rx = -opMode.gamepad1.right_stick_x;
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        frontLeft.setPower(frontLeftPower);
        backLeft.setPower(backLeftPower);
        frontRight.setPower(frontRightPower);
        backRight.setPower(backRightPower);
    }
}
