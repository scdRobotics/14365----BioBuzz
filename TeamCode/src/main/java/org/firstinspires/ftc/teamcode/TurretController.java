package org.firstinspires.ftc.teamcode;

import static com.sun.tools.doclint.Entity.pi;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class TurretController extends ComponentControllerBase
{
    final DistanceUnit inch = DistanceUnit.INCH;
    private static final int PowersPercent = 10;
    public static TurretController singleton;

    private final DcMotorEx turretMotor;
    private final Servo turretServo;

    private int powerPercent = 0;

    Pose2D goalPos = new Pose2D(inch, 12, 12, AngleUnit.DEGREES, 0);

    private boolean aLastTick = false;

    private boolean bLastTick = false;

    private final double rangeOfMotion = 130.90909090909;

    final double f = 18, p = 45;
    final PIDFCoefficients pidfCoefficients = new PIDFCoefficients(p,0,0,f);

    public TurretController(String motorName, String servoName)
    {
        turretMotor = opMode.hardwareMap.get(DcMotorEx.class, motorName);
        turretServo = opMode.hardwareMap.servo.get(servoName);
        turretServo.resetDeviceConfigurationForOpMode();
        turretMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        turretMotor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidfCoefficients);
    }

    public static void createSingleton()
    {
        TurretController.singleton = new TurretController("turret", "pollenServo");
    }

    public void setPower(double power)
    {
        turretMotor.setPower(power);
    }
    public void setVelocity(double velocity)
    {
        turretMotor.setVelocity(velocity);
    }
    public double getVelocity()
    {
        return turretMotor.getVelocity();
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

        final float turretXOffset = -6;
        final float turretYOffset = 2;
        final double turretDistance = 6.324;

        double thetaRobot = goalPos.getHeading(AngleUnit.RADIANS);

        Pose2D pos = Odometry.getPosition();
        double deltaX = pos.getX(inch) - goalPos.getX(inch) + turretXOffset*Math.cos(thetaRobot) - turretYOffset*Math.sin(thetaRobot);
        double deltaY = pos.getY(inch) - (pos.getY(inch) > 0 ? goalPos.getY(inch) : -goalPos.getY(inch)) + turretYOffset*Math.cos(thetaRobot) - turretXOffset*Math.sin(thetaRobot);

        //atan2 returns -pi to pi
        double fieldAngle = Math.atan2(deltaY, deltaX);
        double turretAngle = fieldAngle - thetaRobot;

        //double realAngle = pos.getHeading(AngleUnit.RADIANS) +71*Math.PI/90;
        //double laucherX = pos.getX(inch) + turretDistance*Math.cos(realAngle);
        //double laucherY = pos.getY(inch) + turretDistance*Math.sin(realAngle);

        //0 to 2pi
        turretAngle -= Math.PI;
        double servoAngle = 2.75 * turretAngle;
        //0 to 1
        servoAngle /= (2*Math.PI);

        if(aPressed)
        {
            turretMotor.setVelocity(-830);
        }
        if(bPressed) turretMotor.setVelocity(-300);
        opMode.telemetry.addData("velocity", turretMotor.getVelocity());
        turretServo.setPosition(servoAngle);
    }
}
