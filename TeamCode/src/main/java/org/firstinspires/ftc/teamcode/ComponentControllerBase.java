package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.util.ArrayList;

public abstract class ComponentControllerBase {
    public static ArrayList<ComponentControllerBase> Controllers = new ArrayList<>();
    protected static LinearOpMode opMode;

    public static void initControllers(LinearOpMode mode)
    {
        opMode = mode;
        for (ComponentControllerBase controller : Controllers)
        {
            controller.init();
        }
    }

    public static void tickControllers()
    {
        for (ComponentControllerBase controller : Controllers)
        {
            controller.tick();
        }
    }

    public void tick()
    {
        // does nothing by default
    }

    public void init()
    {
        //does nothing by default
    }
}