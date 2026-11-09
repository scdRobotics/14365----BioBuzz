package org.firstinspires.ftc.teamcode.shooter;

/** Checks for unchanged encoder position without relying on a stale velocity estimate. */
public final class ShooterEncoderStopMonitor {
    private final long requiredStableNanos;
    private boolean hasPosition;
    private int lastPosition;
    private long lastMovementNanos;
    private long stationaryNanos;

    public ShooterEncoderStopMonitor(long requiredStableMillis) {
        if (requiredStableMillis <= 0 || requiredStableMillis > Long.MAX_VALUE / 1_000_000L) {
            throw new IllegalArgumentException("Encoder stop interval must be positive and finite");
        }
        requiredStableNanos = requiredStableMillis * 1_000_000L;
    }

    public void observe(int position, long nowNanos) {
        long elapsed = nowNanos - lastMovementNanos;
        if (!hasPosition || position != lastPosition || elapsed < 0) {
            hasPosition = true;
            lastPosition = position;
            lastMovementNanos = nowNanos;
            stationaryNanos = 0;
        } else {
            stationaryNanos = elapsed;
        }
    }

    public boolean isStopped() {
        return hasPosition && stationaryNanos >= requiredStableNanos;
    }

    public double stationarySeconds() {
        return stationaryNanos / 1_000_000_000.0;
    }

    public void reset() {
        hasPosition = false;
        stationaryNanos = 0;
    }
}
