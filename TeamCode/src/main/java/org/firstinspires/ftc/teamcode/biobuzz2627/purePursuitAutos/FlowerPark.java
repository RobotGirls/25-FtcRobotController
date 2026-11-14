package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;


/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "Flower Park SAFE v3", group = "Pure Pursuit")
public class FlowerPark extends PurePursuitAutoBase<FlowerPark.State> {

    public enum State {
        CLEAR_START,
        STRAFE_TO_PARK,
        END
    }

    @Override
    protected Route route() {
        return BioBuzzRoutes.FLOWER_PARK;
    }

    @Override
    protected State initialState() {
        return State.CLEAR_START;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
            case CLEAR_START:
                runRouteStepThen(0, State.STRAFE_TO_PARK);
                break;
            case STRAFE_TO_PARK:
                runActionThen(this::buildStraightLeftStrafe, State.END);
                break;
            case END:
                drive.stop();
                break;
        }
    }

    /** Build the parking leg directly along the robot's measured left axis. */
    private Action buildStraightLeftStrafe() {
        Pose2d current = drive.getPose();
        double heading = current.heading.toDouble();
        double leftX = -Math.sin(heading);
        double leftY = Math.cos(heading);

        // Choose the robot-left distance that reaches the simulator's parking line. The
        // normal Flower Park heading is -90 degrees, where leftX is +1.
        double leftDistance = Math.abs(leftX) > 0.25
                ? (BioBuzzRoutes.FLOWER_PARK_X - current.position.x) / leftX
                : BioBuzzRoutes.FLOWER_PARK_X + 12;
        if (leftDistance <= 0) {
            leftDistance = BioBuzzRoutes.FLOWER_PARK_X + 12;
        }

        PurePursuitPath strafe = PurePursuitPath.builder()
                .name("straight left into flower parking zone")
                .followDistance(6)
                .moveSpeed(0.3)
                .to(current.position.x + leftX * leftDistance,
                        current.position.y + leftY * leftDistance)
                .fixedHeading(heading)
                .timeout(8)
                .build();
        return guardLeftStrafe(drive.follow(strafe), current, leftX, leftY);
    }

    /**
     * Abort quickly if the supposed strafe is not moving left or is drifting substantially
     * forward. This limits a bad pod direction, loose cable, or stale APK from becoming a run-away.
     */
    private Action guardLeftStrafe(Action follower, Pose2d start, double leftX, double leftY) {
        final long[] startNanos = {-1};
        return packet -> {
            long now = System.nanoTime();
            if (startNanos[0] < 0) {
                startNanos[0] = now;
            }

            boolean running = follower.run(packet);
            if (!running) {
                return false;
            }

            Pose2d pose = drive.getPose();
            double dx = pose.position.x - start.position.x;
            double dy = pose.position.y - start.position.y;
            double leftProgress = dx * leftX + dy * leftY;
            double forwardX = Math.cos(start.heading.toDouble());
            double forwardY = Math.sin(start.heading.toDouble());
            double forwardDrift = dx * forwardX + dy * forwardY;
            double elapsed = (now - startNanos[0]) / 1e9;

            packet.put("guard left progress", leftProgress);
            packet.put("guard forward drift", forwardDrift);
            if (elapsed >= 0.50
                    && (leftProgress < 0.5 || Math.abs(forwardDrift) > 2.0)) {
                drive.stopWithFault(String.format(
                        "Flower strafe failed safety check: left %.1f in, forward %.1f in",
                        leftProgress, forwardDrift));
                packet.put("FAULT", drive.getFault());
                return false;
            }
            return true;
        };
    }
}


