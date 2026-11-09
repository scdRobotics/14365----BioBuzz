package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

public class TurretController extends ComponentControllerBase
{
    private static final int powerstepPercent = 10;
    public static TurretController singleton;

    private final DcMotor turretMotor;

    private int powerPercent = 0;

    private boolean aLastTick = false;

    private boolean bLastTick = false;

    private TurretController(String motorName)
    {
        turretMotor = opMode.hardwareMap.dcMotor.get(motorName);
    }

    public static void createSingleton()
    {
        TurretController.singleton = new TurretController("turret");
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
        boolean aPressed = opMode.gamepad1.a;
        boolean bPressed = opMode.gamepad1.b;
        if (aPressed && !aLastTick)
        {
            powerPercent += powerstepPercent;
        }

        if (bPressed && !bLastTick)
        {
            powerPercent -= powerstepPercent;
        }
        aLastTick = aPressed;
        bLastTick = bPressed;

        powerPercent = Range.clip(powerPercent, -100, 100);
        double power = powerPercent / 100.0;
        opMode.telemetry.addData("Power:", power);
        setPower(power);
        opMode.telemetry.update();
    }
}
