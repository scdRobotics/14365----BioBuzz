package org.firstinspires.ftc.teamcode.shooter;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import java.util.Locale;

/** Independent power test with encoder speed feedback; Pedro drive hardware is not needed. */
@TeleOp(name = "Shooter Test - Manual Power", group = "BIOBUZZ")
public final class ShooterTest extends LinearOpMode {
    private String powerAdvice = "UNAVAILABLE";
    private double ticksPerMotorRev = Double.NaN;
    private double encoderTicksPerSecond = Double.NaN;
    private String encoderScaleSource = "UNSET";
    private String encoderError = "";
    private ShooterEncoderRpm.Reading encoderReading;
    private boolean calibratingEncoder;
    private int calibrationStartTicks;

    @Override
    public void runOpMode() throws InterruptedException {
        DcMotorEx motor;
        try {
            motor = hardwareMap.get(DcMotorEx.class, ShooterTestConfig.MOTOR_NAME);
        } catch (IllegalArgumentException missingMotor) {
            while (!isStarted() && !isStopRequested()) {
                telemetry.addData("DcMotorEx motor not found", ShooterTestConfig.MOTOR_NAME);
                telemetry.addLine("Check MOTOR_NAME and that the motor uses a supported REV controller.");
                telemetry.update();
                sleep(100);
            }
            return;
        }

        motor.setPower(0.0);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setDirection(DcMotor.Direction.REVERSE);
        configureEncoderScale(motor);

        int powerSteps = 0;
        boolean enabled = false;
        double manualRpm = positiveOrZero(ShooterTestConfig.MEASURED_FLYWHEEL_RPM);
        double eta = positiveOrZero(ShooterTestConfig.ETA);
        ShotSnapshot shot = null;
        String status = ShooterTestConfig.USE_ENCODER_RPM
                ? "Ready. Verify encoder scale; wait for stable RPM before locking a shot."
                : "Ready. Enter external flywheel RPM before locking a shot.";
        String geometryError = "";
        ShooterBallistics.Solution solution = null;
        try {
            solution = ShooterBallistics.solve(ShooterTestConfig.LAUNCH_X_METERS,
                    ShooterTestConfig.LAUNCH_HEIGHT_METERS, ShooterTestConfig.TARGET_X_METERS,
                    ShooterTestConfig.TARGET_HEIGHT_METERS, ShooterTestConfig.LAUNCH_ANGLE_DEGREES);
        } catch (IllegalArgumentException invalidGeometry) {
            geometryError = invalidGeometry.getMessage();
        }
        if (solution != null) {
            try {
                ShooterPowerModel powerModel = new ShooterPowerModel(
                        ShooterTestConfig.POWER_REFERENCE_LAUNCH_HEIGHT_METERS,
                        ShooterTestConfig.POWER_REFERENCE_ANGLE_DEGREES,
                        ShooterTestConfig.POWER_REFERENCE_SHOTS);
                double referencePower = powerModel.referencePower(ShooterTestConfig.LAUNCH_HEIGHT_METERS,
                        ShooterTestConfig.LAUNCH_ANGLE_DEGREES, solution);
                powerAdvice = String.format(Locale.US, "%.3f (provisional; apply manually)", referencePower);
            } catch (IllegalArgumentException unavailablePowerModel) {
                powerAdvice = unavailablePowerModel.getMessage();
            }
        }

        try {
            while (!isStarted() && !isStopRequested()) {
                readEncoder(motor);
                showTelemetry(powerSteps, false, manualRpm, eta, solution, geometryError, null, status);
                sleep(50);
            }
            if (isStopRequested()) {
                return;
            }
            Buttons previous = new Buttons(gamepad1);
            while (opModeIsActive()) {
                readEncoder(motor);
                Buttons current = new Buttons(gamepad1);
                int oldPowerSteps = powerSteps;
                double oldRpm = manualRpm;
                boolean stop = current.b;

                if (stop) {
                    powerSteps = 0;
                    enabled = false;
                    shot = null;
                    calibratingEncoder = false;
                    status = "Stopped; shot lock cleared.";
                } else if (ShooterTestConfig.USE_ENCODER_RPM
                        && current.encoderCalibrate && !previous.encoderCalibrate) {
                    if (enabled) {
                        status = "Pause the motor with A or B BEFORE hand-turn calibration.";
                    } else if (!calibratingEncoder && (!Double.isFinite(encoderTicksPerSecond)
                            || Math.abs(encoderTicksPerSecond) > 1.0)) {
                        status = "Wait for the wheel to stop completely before starting hand-turn calibration.";
                    } else {
                        powerSteps = 0;
                        shot = null;
                        try {
                            if (!calibratingEncoder) {
                                calibrationStartTicks = motor.getCurrentPosition();
                                calibratingEncoder = true;
                                status = String.format(Locale.US,
                                        "Motor OFF. Turn CONTACT WHEEL %.1f full turns one way, then press R-stick.",
                                        ShooterTestConfig.ENCODER_CALIBRATION_FLYWHEEL_TURNS);
                            } else {
                                ticksPerMotorRev = ShooterEncoderRpm.calibrateTicksPerMotorRev(
                                        calibrationStartTicks, motor.getCurrentPosition(),
                                        ShooterTestConfig.ENCODER_CALIBRATION_FLYWHEEL_TURNS,
                                        ShooterTestConfig.MOTOR_SPROCKET_TEETH,
                                        ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH);
                                encoderScaleSource = "Hand-turned flywheel calibration";
                                calibratingEncoder = false;
                                readEncoder(motor);
                                status = String.format(Locale.US,
                                        "Scale calibrated: %.6f ticks/motor output rev. Copy it to config.",
                                        ticksPerMotorRev);
                            }
                        } catch (RuntimeException calibrationError) {
                            status = calibrationError.getMessage();
                        }
                    }
                } else if (!calibratingEncoder) {
                    int increment = current.finePower ? 1 : 10;
                    if (current.up && !previous.up) powerSteps += increment;
                    if (current.down && !previous.down) powerSteps -= increment;
                    powerSteps = Math.max(0, Math.min(1000, powerSteps));

                    if (!ShooterTestConfig.USE_ENCODER_RPM) {
                        if (current.right && !previous.right) manualRpm += 10.0;
                        if (current.left && !previous.left) manualRpm -= 10.0;
                        if (current.rb && !previous.rb) manualRpm += 100.0;
                        if (current.lb && !previous.lb) manualRpm -= 100.0;
                    }
                    manualRpm = positiveOrZero(manualRpm);

                    if (oldPowerSteps != powerSteps || oldRpm != manualRpm) {
                        shot = null;
                        status = "Input changed; wait for stable RPM and lock a new shot.";
                    }
                    if (current.a && !previous.a) {
                        enabled = !enabled;
                        if (enabled) {
                            shot = null;
                            status = "Motor enabled. Wait for stable RPM before locking the shot.";
                        }
                    }

                    if (current.y && !previous.y) {
                        if (shot == null) {
                            status = "No locked shot. Press X BEFORE firing, then Y only after a hit.";
                        } else {
                            eta = ShooterBallistics.calibrateEta(shot.ballSpeed,
                                    ShooterTestConfig.FLYWHEEL_RADIUS_METERS, shot.flywheelRpm);
                            shot = null;
                            status = "Hit confirmed: fitted eta updated. Copy it to config for next run.";
                        }
                    } else if (current.x && !previous.x) {
                        if (!enabled || powerSteps == 0) {
                            status = "Enable the motor at the shot power before locking RPM.";
                        } else if (oldPowerSteps != powerSteps || (current.a && !previous.a)) {
                            status = "Wait for stable wheel speed and remeasure RPM before pressing X.";
                        } else if (solution == null) {
                            status = geometryError;
                        } else if (!solution.descendingAtTarget) {
                            status = "Target is not on the descending trajectory; check launch angle.";
                        } else if (ShooterTestConfig.USE_ENCODER_RPM && encoderReading == null) {
                            status = encoderError;
                        } else if (activeFlywheelRpm(manualRpm) <= 0.0) {
                            status = ShooterTestConfig.USE_ENCODER_RPM
                                    ? "No encoder speed: check wiring and allow the wheel to spin up."
                                    : "Enter a positive EXTERNAL measured flywheel RPM first.";
                        } else {
                            // Validate wheel geometry now, before accepting a calibration shot.
                            try {
                                ShooterBallistics.calibrateEta(solution.ballSpeedMetersPerSecond,
                                        ShooterTestConfig.FLYWHEEL_RADIUS_METERS, activeFlywheelRpm(manualRpm));
                                shot = new ShotSnapshot(powerSteps / 1000.0, activeFlywheelRpm(manualRpm),
                                        solution.ballSpeedMetersPerSecond,
                                        ShooterTestConfig.USE_ENCODER_RPM ? "ENCODER" : "EXTERNAL MANUAL");
                                status = "RPM locked. Fire manually; Y confirms a hit at the configured target.";
                            } catch (IllegalArgumentException invalidInput) {
                                status = invalidInput.getMessage();
                            }
                        }
                    }
                }

                motor.setPower(enabled ? powerSteps / 1000.0 : 0.0);
                showTelemetry(powerSteps, enabled, manualRpm, eta, solution, geometryError, shot, status);
                previous = current;
                sleep(20);
            }
        } finally {
            motor.setPower(0.0);
        }
    }

    private void showTelemetry(int powerSteps, boolean enabled, double manualRpm, double eta,
                               ShooterBallistics.Solution solution, String geometryError,
                               ShotSnapshot shot, String status) {
        telemetry.addLine("Up/down: power +/-0.010 | hold LT: +/-0.001 | A: run/pause | B: zero");
        telemetry.addLine(ShooterTestConfig.USE_ENCODER_RPM
                ? "R-stick, motor OFF: begin/end hand-turn encoder calibration"
                : "D-pad left/right: manual RPM +/-10 | LB/RB: manual RPM +/-100");
        telemetry.addLine("X: lock measured pre-shot RPM | Y: confirm actual target hit");
        telemetry.addData("Motor", ShooterTestConfig.MOTOR_NAME);
        telemetry.addData("Power selected / applied", "%.3f / %.3f", powerSteps / 1000.0,
                enabled ? powerSteps / 1000.0 : 0.0);
        if (ShooterTestConfig.USE_ENCODER_RPM) {
            telemetry.addData("Wheel RPM (ENCODER, live)", encoderReading == null
                    ? "UNAVAILABLE" : rpmValue(encoderReading.flywheelRpm));
            telemetry.addData("Motor output RPM (ENCODER)", encoderReading == null
                    ? "UNAVAILABLE" : rpmValue(encoderReading.motorOutputRpm));
            telemetry.addData("Encoder ticks/s (signed)", rpmValue(encoderTicksPerSecond));
            telemetry.addData("Ticks/motor output rev", value(ticksPerMotorRev));
            telemetry.addData("Encoder scale source", encoderScaleSource);
            if (!encoderError.isEmpty()) telemetry.addData("Encoder", encoderError);
            if (calibratingEncoder) telemetry.addLine("HAND-TURN CALIBRATION: motor locked at zero power.");
        } else {
            telemetry.addData("Wheel RPM (external MANUAL)", manualRpm > 0.0 ? value(manualRpm) : "UNSET");
        }
        telemetry.addData("Empirical power reference", powerAdvice);
        telemetry.addLine("Power reference is based on few reported shots, not a guaranteed hit rate.");
        telemetry.addData("Chain teeth: motor -> flywheel", "%d -> %d",
                ShooterTestConfig.MOTOR_SPROCKET_TEETH, ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH);
        telemetry.addData("Launch angle (deg)", value(ShooterTestConfig.LAUNCH_ANGLE_DEGREES));
        telemetry.addData("Launch x / height (m)", "%s / %s",
                value(ShooterTestConfig.LAUNCH_X_METERS), value(ShooterTestConfig.LAUNCH_HEIGHT_METERS));
        telemetry.addData("Target x / height (m)", "%s / %s",
                value(ShooterTestConfig.TARGET_X_METERS), value(ShooterTestConfig.TARGET_HEIGHT_METERS));
        telemetry.addData("Contact wheel radius (m)", value(ShooterTestConfig.FLYWHEEL_RADIUS_METERS));
        telemetry.addData("Ball mass (kg; display only)", value(ShooterTestConfig.BALL_MASS_KG));
        telemetry.addData("Eta (manual or hit fit)", eta > 0.0 ? value(eta) : "UNSET - lock measured RPM + confirm hit");

        if (solution == null) {
            telemetry.addData("Geometry", geometryError);
        } else {
            telemetry.addData("Ideal required ball speed (m/s)", value(solution.ballSpeedMetersPerSecond));
            telemetry.addData("Descending at target", solution.descendingAtTarget ? "YES" : "NO - adjust angle");
            if (eta > 0.0 && solution.descendingAtTarget) {
                try {
                    double targetRpm = ShooterBallistics.requiredFlywheelRpm(
                            solution.ballSpeedMetersPerSecond, ShooterTestConfig.FLYWHEEL_RADIUS_METERS, eta);
                    telemetry.addData("Theoretical target flywheel RPM", value(targetRpm));
                    telemetry.addData("Theoretical target motor output RPM", value(
                            ShooterBallistics.motorRpmForFlywheel(targetRpm,
                                    ShooterTestConfig.MOTOR_SPROCKET_TEETH,
                                    ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH)));
                } catch (IllegalArgumentException invalidInput) {
                    telemetry.addData("Target RPM", invalidInput.getMessage());
                }
            } else {
                telemetry.addData("Theoretical target RPM", "Needs eta and a descending trajectory");
            }
        }
        if (shot != null) {
            telemetry.addData("Locked shot: power / wheel RPM", "%.3f / %.1f (%s)",
                    shot.power, shot.flywheelRpm, shot.rpmSource);
        }
        telemetry.addData("Status", status);
        telemetry.update();
    }

    private static double positiveOrZero(double value) {
        return Double.isFinite(value) && value > 0.0 ? value : 0.0;
    }

    private void configureEncoderScale(DcMotorEx motor) {
        if (!ShooterTestConfig.USE_ENCODER_RPM) return;
        if (!Double.isNaN(ShooterTestConfig.ENCODER_TICKS_PER_MOTOR_REV)) {
            ticksPerMotorRev = ShooterTestConfig.ENCODER_TICKS_PER_MOTOR_REV;
            encoderScaleSource = "Manual ticks/rev override";
            return;
        }
        MotorConfigurationType type = motor.getMotorType();
        if (type == null || "Motor".equals(type.getXmlTag())) {
            encoderScaleSource = "Unspecified motor - R-stick calibration needed";
            return;
        }
        ticksPerMotorRev = type.getTicksPerRev();
        encoderScaleSource = "Robot config (verify actual motor): " + type.getName();
    }

    private void readEncoder(DcMotorEx motor) {
        if (!ShooterTestConfig.USE_ENCODER_RPM) return;
        encoderReading = null;
        encoderTicksPerSecond = Double.NaN;
        try {
            encoderTicksPerSecond = motor.getVelocity();
            encoderReading = ShooterEncoderRpm.fromTicksPerSecond(encoderTicksPerSecond,
                    ticksPerMotorRev, ShooterTestConfig.MOTOR_SPROCKET_TEETH,
                    ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH);
            encoderError = "";
        } catch (RuntimeException readError) {
            encoderError = readError.getMessage() + "; check ticks/rev or use R-stick calibration";
        }
    }

    private double activeFlywheelRpm(double manualRpm) {
        return ShooterTestConfig.USE_ENCODER_RPM
                ? (encoderReading == null ? Double.NaN : encoderReading.flywheelRpm) : manualRpm;
    }

    private static String rpmValue(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.1f", value) : "UNSET";
    }

    private static String value(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.6f", value) : "UNSET";
    }

    private static final class ShotSnapshot {
        final double power;
        final double flywheelRpm;
        final double ballSpeed;
        final String rpmSource;

        ShotSnapshot(double power, double flywheelRpm, double ballSpeed, String rpmSource) {
            this.power = power;
            this.flywheelRpm = flywheelRpm;
            this.ballSpeed = ballSpeed;
            this.rpmSource = rpmSource;
        }
    }

    private static final class Buttons {
        final boolean a, b, x, y, up, down, left, right, lb, rb, finePower, encoderCalibrate;

        Buttons(Gamepad gamepad) {
            a = gamepad.a; b = gamepad.b; x = gamepad.x; y = gamepad.y;
            up = gamepad.dpad_up; down = gamepad.dpad_down;
            left = gamepad.dpad_left; right = gamepad.dpad_right;
            lb = gamepad.left_bumper; rb = gamepad.right_bumper;
            finePower = gamepad.left_trigger > 0.5f;
            encoderCalibrate = gamepad.right_stick_button;
        }
    }
}
