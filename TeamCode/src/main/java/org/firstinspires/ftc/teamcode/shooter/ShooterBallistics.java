package org.firstinspires.ftc.teamcode.shooter;

/** Ideal projectile model. It does not measure ball speed or predict motor power. */
public final class ShooterBallistics {
    private static final double GRAVITY_MPS2 = 9.80665;

    private ShooterBallistics() { }

    public static Solution solve(double launchX, double launchHeight,
                                 double targetX, double targetHeight,
                                 double angleDegrees) {
        if (!Double.isFinite(launchX) || !Double.isFinite(launchHeight)
                || !Double.isFinite(targetX) || !Double.isFinite(targetHeight)
                || !Double.isFinite(angleDegrees)) {
            throw new IllegalArgumentException("Fill launch/target coordinates and angle in config");
        }
        if (angleDegrees <= -90.0 || angleDegrees >= 90.0) {
            throw new IllegalArgumentException("Angle must be between -90 and 90 degrees");
        }
        double dx = targetX - launchX;
        double dy = targetHeight - launchHeight;
        if (!Double.isFinite(dx) || !Double.isFinite(dy) || dx <= 0.0) {
            throw new IllegalArgumentException("Target must have a finite, positive horizontal distance");
        }
        double angle = Math.toRadians(angleDegrees);
        double rise = dx * Math.tan(angle);
        double cos = Math.cos(angle);
        double denominator = 2.0 * cos * cos * (rise - dy);
        if (!Double.isFinite(denominator) || denominator <= 0.0) {
            throw new IllegalArgumentException("Target is unreachable at this launch angle");
        }
        double speed = Math.sqrt(GRAVITY_MPS2 * dx * dx / denominator);
        requirePositive(speed, "Required ball speed");
        return new Solution(speed, rise > 2.0 * dy);
    }

    public static double requiredFlywheelRpm(double ballSpeed, double radius, double eta) {
        requirePositive(ballSpeed, "Ball speed");
        requirePositive(radius, "Flywheel radius");
        requirePositive(eta, "Eta");
        double rpm = 60.0 * ballSpeed / (2.0 * Math.PI * radius * eta);
        requirePositive(rpm, "Required flywheel RPM");
        return rpm;
    }

    public static double calibrateEta(double ballSpeed, double radius, double measuredFlywheelRpm) {
        requirePositive(ballSpeed, "Ball speed");
        requirePositive(radius, "Flywheel radius");
        requirePositive(measuredFlywheelRpm, "External measured flywheel RPM");
        double eta = ballSpeed / (2.0 * Math.PI * radius * measuredFlywheelRpm / 60.0);
        requirePositive(eta, "Calibrated eta");
        return eta;
    }

    public static double motorRpmForFlywheel(double flywheelRpm, int motorTeeth, int flywheelTeeth) {
        requirePositive(flywheelRpm, "Flywheel RPM");
        if (motorTeeth <= 0 || flywheelTeeth <= 0) {
            throw new IllegalArgumentException("Sprocket tooth counts must be positive");
        }
        double rpm = flywheelRpm * flywheelTeeth / motorTeeth;
        requirePositive(rpm, "Motor output RPM");
        return rpm;
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    public static final class Solution {
        public final double ballSpeedMetersPerSecond;
        public final boolean descendingAtTarget;

        private Solution(double ballSpeedMetersPerSecond, boolean descendingAtTarget) {
            this.ballSpeedMetersPerSecond = ballSpeedMetersPerSecond;
            this.descendingAtTarget = descendingAtTarget;
        }
    }
}
