package org.firstinspires.ftc.teamcode.shooter;

/** All experiment inputs use meters, degrees, kilograms, and flywheel RPM. */
public final class ShooterTestConfig {
    private ShooterTestConfig() { }

    // Placeholder: replace with the name in the robot's active configuration.
    public static final String MOTOR_NAME = "shooter";

    // x is horizontal position; y is height, measured from one shared reference.
    public static final double LAUNCH_X_METERS = 0.0;
    public static final double LAUNCH_HEIGHT_METERS = 0.256; // 25.6 cm
    public static final double TARGET_X_METERS = 1.20; // latest target: 120 cm
    public static final double TARGET_HEIGHT_METERS = 1.5494; // 154.94 cm
    public static final double LAUNCH_ANGLE_DEGREES = 75;

    // Contact wheel radius, NOT sprocket radius.
    public static final double FLYWHEEL_RADIUS_METERS = 0.0508;
    public static final int MOTOR_SPROCKET_TEETH = 20;
    public static final int FLYWHEEL_SPROCKET_TEETH = 15;

    // Displayed as experiment metadata. Mass cancels from the ideal trajectory.
    public static final double BALL_MASS_KG = 0.3;

    // Encoder measures speed while setPower still runs in RUN_WITHOUT_ENCODER.
    public static final boolean USE_ENCODER_RPM = true;
    // Ticks per revolution of the MOTOR OUTPUT shaft, including its internal gearbox.
    // NaN uses the configured motor type; an Unspecified Motor is not trusted.
    // Alternatively, calibrate by hand-turning the contact wheel with the motor OFF.
    public static final double ENCODER_TICKS_PER_MOTOR_REV = Double.NaN;
    public static final double ENCODER_CALIBRATION_FLYWHEEL_TURNS = 10.0;

    // External measurement of the contact wheel BEFORE the shot, not motor RPM.
    // Used only when USE_ENCODER_RPM is false. It is never an estimate from power.
    // It can also be entered with the gamepad during the manual-RPM test.
    public static final double MEASURED_FLYWHEEL_RPM = Double.NaN;

    // Leave unset for a first calibration, then copy a confirmed result here.
    public static final double ETA = Double.NaN;

    // Reported hit tests, all at the same 25.6 cm launch height and 75 degree angle.
    // Keep these reference conditions fixed when changing the current target above.
    public static final double POWER_REFERENCE_LAUNCH_HEIGHT_METERS = 0.256;
    public static final double POWER_REFERENCE_ANGLE_DEGREES = 75.0;
    public static final ShooterPowerModel.ReferenceShot[] POWER_REFERENCE_SHOTS = {
            // Horizontal distance (m), target height (m), power, hits, attempts.
            new ShooterPowerModel.ReferenceShot(0.9398, 1.3462, 0.460, 3, 5),
            new ShooterPowerModel.ReferenceShot(0.9398, 1.3462, 0.470, 1, 4),
            new ShooterPowerModel.ReferenceShot(1.2000, 1.5494, 0.480, 1, 2)
    };
}
