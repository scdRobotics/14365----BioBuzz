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

    // Closed-loop setVelocity control requires the motor encoder.
    // This is CONTACT WHEEL RPM, not motor RPM or a 0..1 power fraction.
    public static final double INITIAL_FLYWHEEL_RPM = 0.0;
    // Ticks per revolution of the MOTOR OUTPUT shaft, including its internal gearbox.
    // NaN uses the configured motor type; an Unspecified Motor is not trusted.
    // Alternatively, calibrate by hand-turning the contact wheel with the motor OFF.
    public static final double ENCODER_TICKS_PER_MOTOR_REV = Double.NaN;
    public static final double ENCODER_CALIBRATION_FLYWHEEL_TURNS = 10.0;

    // Leave unset for a first calibration, then copy a confirmed result here.
    public static final double ETA = Double.NaN;

    // Historical open-loop power tests; NOT used to command velocity or estimate RPM.
    // All were at the same 25.6 cm launch height and 75 degree angle.
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
