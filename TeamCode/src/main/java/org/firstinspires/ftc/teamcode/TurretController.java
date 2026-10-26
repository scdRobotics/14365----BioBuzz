package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

public class TurretController extends ComponentControllerBase
{
    public static TurretController singleton;

    private final DcMotor turretMotor;

    private double power = 0;

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
        if (opMode.gamepad1.a && !aLastTick)
        {
            power += 10;
            aLastTick = true;
        }
        else
        {
            aLastTick = false;
        }

        if (opMode.gamepad1.b && !bLastTick)
        {
            power -= 10;
            bLastTick = true;
        }
        else
        {
            bLastTick = false;
        }

        opMode.telemetry.addData("Power:", power);


        setPower(power);
        opMode.telemetry.update();
    }
}
