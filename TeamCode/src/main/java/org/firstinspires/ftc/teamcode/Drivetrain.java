package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Drivetrain extends ComponentControllerBase{
    public static Drivetrain singleton;
    private final DcMotor frontLeft;
    private final DcMotor backLeft;
    private final DcMotor frontRight;
    private final DcMotor backRight;

    private final IMU imu;

    private Drivetrain(String frontLeft, String frontRight, String backLeft, String backRight)
    {
        this.frontLeft = opMode.hardwareMap.dcMotor.get(frontLeft);
        this.frontRight = opMode.hardwareMap.dcMotor.get(frontRight);
        this.backLeft = opMode.hardwareMap.dcMotor.get(backLeft);
        this.backRight = opMode.hardwareMap.dcMotor.get(backRight);
        this.imu = opMode.hardwareMap.get(IMU.class, "imu");

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
        double y = opMode.gamepad1.left_stick_y; // Remember, Y stick is reversed!
        double x = -opMode.gamepad1.left_stick_x;
        double rx = opMode.gamepad1.right_stick_x;

        frontLeft.setPower(y + x + rx);
        backLeft.setPower(y - x + rx);
        frontRight.setPower(y - x - rx);
        backRight.setPower(y + x - rx);
    }
}
