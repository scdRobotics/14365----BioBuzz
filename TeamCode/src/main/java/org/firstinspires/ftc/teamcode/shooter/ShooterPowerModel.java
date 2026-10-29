package org.firstinspires.ftc.teamcode.shooter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Local empirical power guidance using model-equivalent ball speeds at tested targets.
 * A reference speed is inferred from hit geometry, not measured. This is not a motor
 * speed controller, an eta measurement, or a prediction of the true hit probability.
 */
public final class ShooterPowerModel {
    private static final double TOLERANCE = 1e-9;
    private final double referenceLaunchHeight;
    private final double referenceAngle;
    private final List<Anchor> anchors;

    public ShooterPowerModel(double launchHeight, double angle, ReferenceShot... observations) {
        if (!Double.isFinite(launchHeight) || !Double.isFinite(angle)
                || observations == null) {
            throw new IllegalArgumentException("Invalid reference launch geometry");
        }
        referenceLaunchHeight = launchHeight;
        referenceAngle = angle;
        List<ReferenceShot> bestAtEachTarget = new ArrayList<>();
        for (ReferenceShot observation : observations) {
            validate(observation);
            ShooterBallistics.Solution solution = ShooterBallistics.solve(0.0, launchHeight,
                    observation.distanceMeters, observation.targetHeightMeters, angle);
            if (!solution.descendingAtTarget) {
                throw new IllegalArgumentException("Reference hit must be on a descending trajectory");
            }
            if (observation.hits == 0) continue;
            int matchingTarget = -1;
            for (int i = 0; i < bestAtEachTarget.size(); i++) {
                ReferenceShot other = bestAtEachTarget.get(i);
                if (same(other.distanceMeters, observation.distanceMeters)
                        && same(other.targetHeightMeters, observation.targetHeightMeters)) {
                    matchingTarget = i;
                    break;
                }
            }
            if (matchingTarget < 0) {
                bestAtEachTarget.add(observation);
            } else if (betterReportedResult(observation, bestAtEachTarget.get(matchingTarget))) {
                bestAtEachTarget.set(matchingTarget, observation);
            }
        }
        if (bestAtEachTarget.size() < 2) {
            throw new IllegalArgumentException("Need hit references at two distinct targets");
        }
        anchors = new ArrayList<>();
        for (ReferenceShot reference : bestAtEachTarget) {
            double speed = ShooterBallistics.solve(0.0, launchHeight, reference.distanceMeters,
                    reference.targetHeightMeters, angle).ballSpeedMetersPerSecond;
            anchors.add(new Anchor(speed, reference.power));
        }
        anchors.sort(Comparator.comparingDouble(anchor -> anchor.speed));
        for (int i = 1; i < anchors.size(); i++) {
            Anchor previous = anchors.get(i - 1);
            Anchor current = anchors.get(i);
            if (current.speed - previous.speed <= TOLERANCE || current.power < previous.power) {
                throw new IllegalArgumentException("Reference powers are inconsistent; collect more tests");
            }
        }
    }

    public double referencePower(double launchHeight, double angle,
                                 ShooterBallistics.Solution target) {
        if (!same(launchHeight, referenceLaunchHeight) || !same(angle, referenceAngle)) {
            throw new IllegalArgumentException("Launch height/angle changed: new power references needed");
        }
        if (target == null || !target.descendingAtTarget) {
            throw new IllegalArgumentException("Power reference requires a descending target trajectory");
        }
        double speed = target.ballSpeedMetersPerSecond;
        if (speed < anchors.get(0).speed - TOLERANCE
                || speed > anchors.get(anchors.size() - 1).speed + TOLERANCE) {
            throw new IllegalArgumentException("Outside tested speed range: add hit references");
        }
        for (Anchor anchor : anchors) {
            if (same(speed, anchor.speed)) return anchor.power;
        }
        for (int i = 1; i < anchors.size(); i++) {
            Anchor lower = anchors.get(i - 1);
            Anchor upper = anchors.get(i);
            if (speed <= upper.speed) {
                double fraction = (speed - lower.speed) / (upper.speed - lower.speed);
                return lower.power + fraction * (upper.power - lower.power);
            }
        }
        throw new IllegalArgumentException("Outside tested speed range");
    }

    private static boolean betterReportedResult(ReferenceShot candidate, ReferenceShot current) {
        // Rank observed hit fractions only; small samples do NOT establish superiority.
        long candidateScore = (long) candidate.hits * current.attempts;
        long currentScore = (long) current.hits * candidate.attempts;
        if (candidateScore != currentScore) return candidateScore > currentScore;
        if (candidate.attempts != current.attempts) return candidate.attempts > current.attempts;
        return candidate.power < current.power;
    }

    private static void validate(ReferenceShot observation) {
        if (observation == null || !Double.isFinite(observation.power)
                || observation.power <= 0.0 || observation.power > 1.0
                || observation.attempts <= 0 || observation.hits < 0
                || observation.hits > observation.attempts) {
            throw new IllegalArgumentException("Invalid power reference or hit counts");
        }
    }

    private static boolean same(double first, double second) {
        return Double.isFinite(first) && Double.isFinite(second)
                && Math.abs(first - second) <= TOLERANCE;
    }

    public static final class ReferenceShot {
        public final double distanceMeters;
        public final double targetHeightMeters;
        public final double power;
        public final int hits;
        public final int attempts;

        public ReferenceShot(double distanceMeters, double targetHeightMeters,
                             double power, int hits, int attempts) {
            this.distanceMeters = distanceMeters;
            this.targetHeightMeters = targetHeightMeters;
            this.power = power;
            this.hits = hits;
            this.attempts = attempts;
        }
    }

    private static final class Anchor {
        final double speed;
        final double power;

        Anchor(double speed, double power) {
            this.speed = speed;
            this.power = power;
        }
    }
}
