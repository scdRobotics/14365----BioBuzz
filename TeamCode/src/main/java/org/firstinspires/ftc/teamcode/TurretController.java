package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

public class TurretController extends ComponentControllerBase
{
    public static TurretController singleton;

    private final DcMotor turretMotor;

    private TurretController(String motorName)
    {
        turretMotor = opMode.hardwareMap.dcMotor.get(motorName);
    }

    public static void setSingleton(TurretController singleton)
    {
        TurretController.singleton = new TurretController("turret");
    }

    public void setPower(float power)
    {
        turretMotor.setPower(power);
    }

    public void stopTurret()
    {
        turretMotor.setPower(0);
    }
}
