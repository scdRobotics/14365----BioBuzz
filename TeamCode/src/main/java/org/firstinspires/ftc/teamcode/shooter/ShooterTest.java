package org.firstinspires.ftc.teamcode.shooter;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

import java.util.Locale;

/** Independent closed-loop flywheel RPM test; Pedro drive hardware is not needed. */
@TeleOp(name = "Shooter Test - Velocity", group = "BIOBUZZ")
public final class ShooterTest extends LinearOpMode {
    private double ticksPerMotorRev = Double.NaN;
    private double encoderTicksPerSecond = Double.NaN;
    private String encoderScaleSource = "UNSET";
    private String encoderError = "";
    private String velocityPidf = "UNAVAILABLE";
    private final ShooterEncoderStopMonitor stopMonitor = new ShooterEncoderStopMonitor(500);
    private Integer encoderPosition;
    private String encoderPositionError = "";
    private boolean motorOutputEnabled;
    private int calibrationRequests;
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

        motor.setVelocity(0.0);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor.setDirection(DcMotor.Direction.REVERSE);
        motor.setMotorDisable();
        configureEncoderScale(motor);
        readVelocityPidf(motor);

        double targetRpm = positiveOrZero(ShooterTestConfig.INITIAL_FLYWHEEL_RPM);
        boolean enabled = false;
        double eta = positiveOrZero(ShooterTestConfig.ETA);
        ShotSnapshot shot = null;
        String status = "Ready. Select wheel RPM; verify encoder scale before running.";
        String geometryError = "";
        ShooterBallistics.Solution solution = null;
        try {
            solution = ShooterBallistics.solve(ShooterTestConfig.LAUNCH_X_METERS,
                    ShooterTestConfig.LAUNCH_HEIGHT_METERS, ShooterTestConfig.TARGET_X_METERS,
                    ShooterTestConfig.TARGET_HEIGHT_METERS, ShooterTestConfig.LAUNCH_ANGLE_DEGREES);
        } catch (IllegalArgumentException invalidGeometry) {
            geometryError = invalidGeometry.getMessage();
        }
        try {
            while (!isStarted() && !isStopRequested()) {
                readEncoder(motor);
                showTelemetry(targetRpm, false, 0.0, eta, solution, geometryError, null, status);
                sleep(50);
            }
            if (isStopRequested()) {
                return;
            }
            Buttons previous = new Buttons(gamepad1);
            while (opModeIsActive()) {
                readEncoder(motor);
                Buttons current = new Buttons(gamepad1);
                double oldTargetRpm = targetRpm;
                boolean stop = current.b;
                boolean calibrationRequested = current.encoderCalibrate && !previous.encoderCalibrate;
                if (calibrationRequested) calibrationRequests++;

                if (stop) {
                    targetRpm = 0.0;
                    enabled = false;
                    shot = null;
                    calibratingEncoder = false;
                    status = current.encoderCalibrate
                            ? "B has priority. Release B and RB/R1, then press RB/R1 to calibrate."
                            : "Output disabled; shot lock cleared. Release B; press RB/R1 when stopped.";
                } else if (calibrationRequested) {
                    if (enabled) {
                        status = "Pause the motor with A or B BEFORE hand-turn calibration.";
                    } else if (encoderPosition == null) {
                        status = "Cannot calibrate: " + encoderPositionError;
                    } else if (!calibratingEncoder && !stopMonitor.isStopped()) {
                        status = "Wait for encoder POSITION to stay still 0.5 s, then release/press RB/R1 again.";
                    } else {
                        targetRpm = 0.0;
                        shot = null;
                        try {
                            if (!calibratingEncoder) {
                                disableMotor(motor);
                                calibrationStartTicks = motor.getCurrentPosition();
                                calibratingEncoder = true;
                                status = String.format(Locale.US,
                                        "Output DISABLED. Turn CONTACT WHEEL %.1f full turns one way, then press RB/R1.",
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
                    if (current.up && !previous.up) targetRpm += 10.0;
                    if (current.down && !previous.down) targetRpm -= 10.0;
                    if (current.right && !previous.right) targetRpm += 3.0;
                    if (current.left && !previous.left) targetRpm -= 3.0;
                    // Round to a tenth to prevent accumulated fine-step drift.
                    targetRpm = Math.max(0.0, Math.round(targetRpm * 10.0) / 10.0);

                    if (oldTargetRpm != targetRpm) {
                        shot = null;
                        status = "Input changed; wait for stable RPM and lock a new shot.";
                    }
                    if (current.a && !previous.a) {
                        enabled = !enabled;
                        if (enabled) {
                            shot = null;
                            status = "Velocity control enabled. Wait for stable RPM before locking the shot.";
                        } else {
                            status = "Output disabled; target RPM and any locked shot retained.";
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
                        if (!enabled || targetRpm == 0.0) {
                            status = "Enable the motor at the target RPM before locking a shot.";
                        } else if (oldTargetRpm != targetRpm || (current.a && !previous.a)) {
                            status = "Wait for stable wheel speed and remeasure RPM before pressing X.";
                        } else if (solution == null) {
                            status = geometryError;
                        } else if (!solution.descendingAtTarget) {
                            status = "Target is not on the descending trajectory; check launch angle.";
                        } else if (encoderReading == null) {
                            status = encoderError;
                        } else if (encoderReading.flywheelRpm <= 0.0) {
                            status = "No encoder speed: check wiring and allow the wheel to spin up.";
                        } else {
                            // Validate wheel geometry now, before accepting a calibration shot.
                            try {
                                ShooterBallistics.calibrateEta(solution.ballSpeedMetersPerSecond,
                                        ShooterTestConfig.FLYWHEEL_RADIUS_METERS, encoderReading.flywheelRpm);
                                shot = new ShotSnapshot(targetRpm, encoderReading.flywheelRpm,
                                        solution.ballSpeedMetersPerSecond);
                                status = "RPM locked. Fire manually; Y confirms a hit at the configured target.";
                            } catch (IllegalArgumentException invalidInput) {
                                status = invalidInput.getMessage();
                            }
                        }
                    }
                }

                double commandTicksPerSecond = 0.0;
                if (enabled) {
                    try {
                        if (encoderReading == null) {
                            throw new IllegalArgumentException(encoderError);
                        }
                        if (targetRpm <= 0.0) {
                            throw new IllegalArgumentException("Select a positive target wheel RPM before pressing A.");
                        }
                        commandTicksPerSecond = ShooterEncoderRpm.targetTicksPerSecond(targetRpm,
                                ticksPerMotorRev, ShooterTestConfig.MOTOR_SPROCKET_TEETH,
                                ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH);
                    } catch (IllegalArgumentException invalidVelocity) {
                        enabled = false;
                        shot = null;
                        status = "Motor OFF: " + invalidVelocity.getMessage();
                    }
                }
                if (enabled) {
                    // Mark before the call so finally also disables output if the command fails.
                    motorOutputEnabled = true;
                    motor.setVelocity(commandTicksPerSecond);
                } else {
                    // Zero velocity is an active loop. Keep paused/calibrating output disabled.
                    disableMotor(motor);
                }
                showTelemetry(targetRpm, enabled, commandTicksPerSecond, eta,
                        solution, geometryError, shot, status);
                previous = current;
                sleep(20);
            }
        } finally {
            disableMotor(motor);
        }
    }

    private void showTelemetry(double targetRpm, boolean enabled, double commandTicksPerSecond, double eta,
                               ShooterBallistics.Solution solution, String geometryError,
                               ShotSnapshot shot, String status) {
        telemetry.addData("Control state", calibratingEncoder ? "HAND-TURN CALIBRATION - OUTPUT DISABLED"
                : enabled ? "RUNNING" : "PAUSED - OUTPUT DISABLED");
        telemetry.addData("Status", status);
        telemetry.addData("Calibration (RB/R1)", calibrationHint(enabled));
        telemetry.addData("RB/R1 calibration presses received", calibrationRequests);
        telemetry.addData("Encoder position / stationary seconds", "%s / %.2f",
                encoderPosition == null ? "UNAVAILABLE" : encoderPosition.toString(),
                stopMonitor.stationarySeconds());
        if (calibratingEncoder && encoderPosition != null) {
            telemetry.addData("Hand-turn encoder counts", (long) (encoderPosition - calibrationStartTicks));
        }
        if (!encoderPositionError.isEmpty()) telemetry.addData("Encoder position error", encoderPositionError);
        telemetry.addData("Motor", ShooterTestConfig.MOTOR_NAME);
        telemetry.addData("Target wheel RPM selected / commanded", "%.1f / %.1f", targetRpm,
                enabled ? targetRpm : 0.0);
        telemetry.addData("Motor velocity command (ticks/s)", "%.1f", commandTicksPerSecond);
        telemetry.addData("Wheel RPM (ENCODER, live)", encoderReading == null
                ? "UNAVAILABLE" : rpmValue(encoderReading.flywheelRpm));
        telemetry.addData("Motor output RPM (ENCODER)", encoderReading == null
                ? "UNAVAILABLE" : rpmValue(encoderReading.motorOutputRpm));
        telemetry.addData("Wheel RPM error (command - actual)", encoderReading == null
                ? "UNAVAILABLE" : rpmValue((enabled ? targetRpm : 0.0) - encoderReading.flywheelRpm));
        telemetry.addData("Encoder ticks/s (signed)", rpmValue(encoderTicksPerSecond));
        telemetry.addData("Ticks/motor output rev", value(ticksPerMotorRev));
        telemetry.addData("Encoder scale source", encoderScaleSource);
        telemetry.addData("Hub velocity PIDF (current)", velocityPidf);
        if (!encoderError.isEmpty()) telemetry.addData("Encoder", encoderError);
        if (calibratingEncoder) telemetry.addLine("HAND-TURN CALIBRATION: motor output DISABLED.");
        telemetry.addLine("D-pad up/down: RPM +/-10 | right/left: +/-3 | A: run/pause | B: zero");
        telemetry.addLine("RB/R1 (right SHOULDER button): begin/end calibration, motor OFF");
        telemetry.addLine("X: lock measured pre-shot RPM | Y: confirm actual target hit");
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
                    double theoreticalRpm = ShooterBallistics.requiredFlywheelRpm(
                            solution.ballSpeedMetersPerSecond, ShooterTestConfig.FLYWHEEL_RADIUS_METERS, eta);
                    telemetry.addData("Theoretical target flywheel RPM", value(theoreticalRpm));
                    telemetry.addData("Theoretical target motor output RPM", value(
                            ShooterBallistics.motorRpmForFlywheel(theoreticalRpm,
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
            telemetry.addData("Locked shot: target / measured wheel RPM", "%.1f / %.1f",
                    shot.targetRpm, shot.flywheelRpm);
        }
        telemetry.update();
    }

    private static double positiveOrZero(double value) {
        return Double.isFinite(value) && value > 0.0 ? value : 0.0;
    }

    private String calibrationHint(boolean enabled) {
        if (!isStarted()) return "Tap START on the Driver Station. Press B to stop the shooter, then release B.";
        if (enabled) return "Step 1. Press B to stop the shooter, then release B.";
        if (gamepad1.b) return "Release B before pressing the right shoulder button (RB/R1).";
        if (calibratingEncoder) {
            return String.format(Locale.US, "Step 2. Turn the flywheel %.1f revolutions by hand, then press RB/R1 to finish.",
                    ShooterTestConfig.ENCODER_CALIBRATION_FLYWHEEL_TURNS);
        }
        if (encoderPosition == null) return "Could not read the encoder position. Check Status.";
        if (!stopMonitor.isStopped()) return "Wait for the flywheel to stop. Press RB/R1 when READY appears.";
        if ("Hand-turned flywheel calibration".equals(encoderScaleSource)) {
            return "Calibration complete. Record the ticks per motor output revolution. Press RB/R1 to recalibrate.";
        }
        return "READY. Press and release the right shoulder button (RB/R1) to begin calibration.";
    }

    private void configureEncoderScale(DcMotorEx motor) {
        if (!Double.isNaN(ShooterTestConfig.ENCODER_TICKS_PER_MOTOR_REV)) {
            ticksPerMotorRev = ShooterTestConfig.ENCODER_TICKS_PER_MOTOR_REV;
            encoderScaleSource = "Manual ticks/rev override";
            return;
        }
        MotorConfigurationType type = motor.getMotorType();
        if (type == null || "Motor".equals(type.getXmlTag())) {
            encoderScaleSource = "Unspecified motor - RB/R1 calibration needed";
            return;
        }
        ticksPerMotorRev = type.getTicksPerRev();
        encoderScaleSource = "Robot config (verify actual motor): " + type.getName();
    }

    private void readVelocityPidf(DcMotorEx motor) {
        try {
            PIDFCoefficients coefficients = motor.getPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
            velocityPidf = coefficients == null ? "UNAVAILABLE" : String.format(Locale.US,
                    "P=%.6g I=%.6g D=%.6g F=%.6g (%s)", coefficients.p, coefficients.i,
                    coefficients.d, coefficients.f, coefficients.algorithm);
        } catch (RuntimeException readError) {
            velocityPidf = "UNAVAILABLE: " + readError.getMessage();
        }
    }

    private void disableMotor(DcMotorEx motor) {
        try {
            // Clear an old running target once; setVelocity itself re-enables the bridge.
            if (motorOutputEnabled) motor.setVelocity(0.0);
        } finally {
            motor.setMotorDisable();
            if (motorOutputEnabled) stopMonitor.reset();
            motorOutputEnabled = false;
        }
    }

    private void readEncoder(DcMotorEx motor) {
        encoderReading = null;
        encoderTicksPerSecond = Double.NaN;
        try {
            encoderTicksPerSecond = motor.getVelocity();
            encoderReading = ShooterEncoderRpm.fromTicksPerSecond(encoderTicksPerSecond,
                    ticksPerMotorRev, ShooterTestConfig.MOTOR_SPROCKET_TEETH,
                    ShooterTestConfig.FLYWHEEL_SPROCKET_TEETH);
            encoderError = "";
        } catch (RuntimeException readError) {
            encoderError = readError.getMessage()
                    + "; check ticks/rev or use RB/R1 calibration";
        }
        try {
            encoderPosition = motor.getCurrentPosition();
            stopMonitor.observe(encoderPosition, System.nanoTime());
            encoderPositionError = "";
        } catch (RuntimeException positionError) {
            encoderPosition = null;
            stopMonitor.reset();
            encoderPositionError = "Cannot read encoder position: " + positionError.getMessage();
        }
    }

    private static String rpmValue(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.1f", value) : "UNSET";
    }

    private static String value(double value) {
        return Double.isFinite(value) ? String.format(Locale.US, "%.6f", value) : "UNSET";
    }

    private static final class ShotSnapshot {
        final double targetRpm;
        final double flywheelRpm;
        final double ballSpeed;

        ShotSnapshot(double targetRpm, double flywheelRpm, double ballSpeed) {
            this.targetRpm = targetRpm;
            this.flywheelRpm = flywheelRpm;
            this.ballSpeed = ballSpeed;
        }
    }

    private static final class Buttons {
        final boolean a, b, x, y, up, down, left, right, encoderCalibrate;

        Buttons(Gamepad gamepad) {
            a = gamepad.a; b = gamepad.b; x = gamepad.x; y = gamepad.y;
            up = gamepad.dpad_up; down = gamepad.dpad_down;
            left = gamepad.dpad_left; right = gamepad.dpad_right;
            encoderCalibrate = gamepad.right_bumper;
        }
    }
}
