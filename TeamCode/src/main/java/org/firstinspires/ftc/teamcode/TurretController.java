package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class TurretController extends ComponentControllerBase
{
    //public static TurretController singleton;

    private final DcMotorEx turretMotor;
    private final DcMotor transferMotor;

    private double power = -900;

    private boolean aLastTick = false;

    private boolean bLastTick = false;

    private TurretController(String turretName, String transferName)
    {
        turretMotor = opMode.hardwareMap.get(DcMotorEx.class, turretName);
        transferMotor = opMode.hardwareMap.dcMotor.get(transferName);
    }

    public static TurretController createSingleton()
    {
        return new TurretController("turret", "transfer");
    }

    //ticks per rotation
    public void setPower(double power)
    {
        turretMotor.setVelocity(power);
    }

    public void transfer() { transferMotor.setPower(-1); }

    public void stopTurret()
    {
        turretMotor.setVelocity(0);
        transferMotor.setPower(0);
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

        if(opMode.gamepad1.right_bumper) transfer();
        if(opMode.gamepad1.left_bumper) setPower(power);
        else stopTurret();
        opMode.telemetry.update();
    }
}
