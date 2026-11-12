package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.hardware.DcMotor;

public class Transfer extends ComponentControllerBase {
    public static Transfer singleton;
    DcMotor transfer;
    private Transfer(String motorName)
    {
        this.transfer = opMode.hardwareMap.dcMotor.get(motorName);
    }

    @Override
    public void tick()
    {
        double upPower = Math.min(opMode.gamepad2.right_trigger, 0.6);
        double downPower = Math.min(opMode.gamepad2.left_trigger, 0.6);

        double totPower = upPower-downPower;
        transfer.setPower(totPower);
    }
    public static void createSingleton()
    {
        singleton = new Transfer("transfer");
    }
}
