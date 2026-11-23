package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorGoBildaPinpoint;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose2D;

public class Odometry extends ComponentControllerBase
{
    public static Odometry singleton;
    public GoBildaPinpointDriver odo = null;
    private Odometry()
    {
        odo = opMode.hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");
    }
    public static void createSingleton()
    {
        singleton = new Odometry();
    }

    private static Pose2D pos = new Pose2D(DistanceUnit.INCH, 0,0, AngleUnit.DEGREES,0);

    public static Pose2D getPosition()
    {
        return pos;
    }

    @Override
    public void tick()
    {
        odo.update();
        opMode.telemetry.addData("position x", odo.getPosX(DistanceUnit.INCH));
        opMode.telemetry.addData("position y", odo.getPosY(DistanceUnit.INCH));
        opMode.telemetry.addData("heading", odo.getHeading(AngleUnit.DEGREES));

        if(opMode.gamepad1.yWasPressed())
        {
            setPosition(new Pose2D(DistanceUnit.INCH, -59.77,65.07, AngleUnit.DEGREES, 0));
        }
        if(opMode.gamepad1.bWasPressed())
        {
            setPosition(new Pose2D(DistanceUnit.INCH, -59.77,65.07, AngleUnit.DEGREES, -90));
        }
        if(opMode.gamepad1.xWasPressed())
        {
            setPosition(new Pose2D(DistanceUnit.INCH, -59.77,65.07, AngleUnit.DEGREES, 90));
        }
        if(opMode.gamepad1.aWasPressed())
        {
            setPosition(new Pose2D(DistanceUnit.INCH, -59.77,65.07, AngleUnit.DEGREES, 180));
        }

        pos = odo.getPosition();
    }

    public void setOffsets(double xOffset, double yOffset)
    {
        odo.setOffsets(xOffset, yOffset, DistanceUnit.INCH);
    }
    public void resetPosAndIMU()
    {
        long startTime = System.currentTimeMillis();
        odo.resetPosAndIMU();
        while(startTime + 300 < System.currentTimeMillis());
    }

    public void setPosition(Pose2D pos)
    {
        odo.setPosition(pos);
    }

    @Override
    public void init()
    {
        resetPosAndIMU();
        odo.setEncoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        odo.setOffsets(-7.375, 2.75, DistanceUnit.INCH);
        odo.setEncoderDirections(GoBildaPinpointDriver.EncoderDirection.FORWARD, GoBildaPinpointDriver.EncoderDirection.REVERSED);


    }
}
