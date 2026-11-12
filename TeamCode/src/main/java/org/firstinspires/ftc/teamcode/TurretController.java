package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

public class TurretController extends ComponentControllerBase
{
    private static final int PowersPercent = 10;
    public static TurretController singleton;

    private final DcMotorEx turretMotor;
    private final Servo turretServo;

    private int powerPercent = 0;

    private boolean aLastTick = false;

    private boolean bLastTick = false;

    private TurretController(String motorName, String servoName)
    {
        turretMotor = opMode.hardwareMap.get(DcMotorEx.class, motorName);
        turretServo = opMode.hardwareMap.servo.get(servoName);

        turretServo.resetDeviceConfigurationForOpMode();
    }

    public static void createSingleton()
    {
        TurretController.singleton = new TurretController("turret", "pollenServo");
    }

    public void setPower(double power)
    {
        turretMotor.setPower(power);
    }

    public void stopTurret()
    {
        turretMotor.setPower(0);
    }

    @Override
    public void tick()
    {
        boolean aPressed = opMode.gamepad2.a;
        boolean bPressed = opMode.gamepad2.b;

        double angle = opMode.gamepad2.right_stick_x;
        if(aPressed)
        {
            turretMotor.setVelocity(-830);
        }
        if(bPressed) turretMotor.setVelocity(-300);
        opMode.telemetry.addData("velocity", turretMotor.getVelocity());
        opMode.telemetry.update();
        turretServo.setPosition(angle);
    }
}
