package org.firstinspires.ftc.teamcode.shooter;

/** Encoder conversion uses motor OUTPUT revolutions; do not apply internal gearing again. */
public final class ShooterEncoderRpm {
    private ShooterEncoderRpm() { }

    public static Reading fromTicksPerSecond(double signedTicksPerSecond,
                                             double ticksPerMotorRev,
                                             int motorTeeth, int flywheelTeeth) {
        requirePositive(ticksPerMotorRev, "Encoder ticks per motor output revolution");
        requireTeeth(motorTeeth, flywheelTeeth);
        if (!Double.isFinite(signedTicksPerSecond)) {
            throw new IllegalArgumentException("Encoder velocity is not finite");
        }
        double motorRpm = Math.abs(signedTicksPerSecond) / ticksPerMotorRev * 60.0;
        double flywheelRpm = motorRpm * motorTeeth / flywheelTeeth;
        if (!Double.isFinite(motorRpm) || !Double.isFinite(flywheelRpm)) {
            throw new IllegalArgumentException("Encoder RPM conversion overflow");
        }
        return new Reading(signedTicksPerSecond, motorRpm, flywheelRpm);
    }

    public static double calibrateTicksPerMotorRev(int startTicks, int endTicks,
                                                  double flywheelTurns,
                                                  int motorTeeth, int flywheelTeeth) {
        requirePositive(flywheelTurns, "Hand-turned flywheel revolutions");
        requireTeeth(motorTeeth, flywheelTeeth);
        // int subtraction handles crossing the signed 32-bit counter boundary.
        int delta = endTicks - startTicks;
        double movedTicks = Math.abs((long) delta);
        if (movedTicks == 0.0) {
            throw new IllegalArgumentException("No encoder counts: check wiring, then retry calibration");
        }
        double ticksPerMotorRev = movedTicks / flywheelTurns * motorTeeth / flywheelTeeth;
        requirePositive(ticksPerMotorRev, "Calibrated ticks per motor output revolution");
        return ticksPerMotorRev;
    }

    private static void requirePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static void requireTeeth(int motorTeeth, int flywheelTeeth) {
        if (motorTeeth <= 0 || flywheelTeeth <= 0) {
            throw new IllegalArgumentException("Sprocket tooth counts must be positive");
        }
    }

    public static final class Reading {
        public final double signedTicksPerSecond;
        public final double motorOutputRpm;
        public final double flywheelRpm;

        private Reading(double signedTicksPerSecond, double motorOutputRpm, double flywheelRpm) {
            this.signedTicksPerSecond = signedTicksPerSecond;
            this.motorOutputRpm = motorOutputRpm;
            this.flywheelRpm = flywheelRpm;
        }
    }
}
