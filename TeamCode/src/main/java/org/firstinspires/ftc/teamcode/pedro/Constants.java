package org.firstinspires.ftc.teamcode.pedro;

import com.pedropathing.follower.Follower;
import com.pedropathing.revhub.localizers.PinpointConfig;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Constants {
    public static Follower create(HardwareMap h) {
        // return new Follower(Drivetrain, Localizer, Foresight);
        return null;
    }

    public static PinpointConfig localizerConfig = new PinpointConfig(pinpointConfig -> {
        pinpointConfig.name.set("pinpoint");
        pinpointConfig.xPodOffset.set(0.0);
        pinpointConfig.yPodOffset.set(0.0);

        pinpointConfig.xPodDirection.set(
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );

        pinpointConfig.yPodDirection.set(
                GoBildaPinpointDriver.EncoderDirection.FORWARD
        );
    });
}