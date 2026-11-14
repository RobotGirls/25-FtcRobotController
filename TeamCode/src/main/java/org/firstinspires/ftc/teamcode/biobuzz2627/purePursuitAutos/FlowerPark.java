package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;

import org.firstinspires.ftc.teamcode.purepursuit.PurePursuitPath;
import org.firstinspires.ftc.teamcode.purepursuit.Route;
import org.firstinspires.ftc.teamcode.purepursuit.paths.BioBuzzRoutes;


/** Pure pursuit version of biobuzzautos/blueGardenAuto. */
@Autonomous(name = "Flower Park", group = "Pure Pursuit")
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
        return State.STRAFE_TO_PARK;
    }

    @Override
    protected void mainStateMachine() {
        switch (getState()) {
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

        // Choose the robot-left distance that reaches the simulator's X=34 parking line. The
        // normal Flower Park heading is -90 degrees, where leftX is +1.
        double leftDistance = Math.abs(leftX) > 0.25
                ? (34 - current.position.x) / leftX
                : 46;
        if (leftDistance <= 0) {
            leftDistance = 46;
        }

        PurePursuitPath strafe = PurePursuitPath.builder()
                .name("straight left into flower parking zone")
                .followDistance(6)
                .moveSpeed(0.5)
                .to(current.position.x + leftX * leftDistance,
                        current.position.y + leftY * leftDistance)
                .fixedHeading(heading)
                .timeout(8)
                .build();
        return drive.follow(strafe);
    }
}


