package org.firstinspires.ftc.teamcode.biobuzz2627.auto;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.DynamicPurePursuitFollower;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.statemachine.StateMachineAutoBase;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.FieldMap;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.GoalSelector;
import org.ftc.hiveplanner.Point;

import java.util.List;

/**
 * Safe, disabled example of generated Pure Pursuit inside the iterative state-machine base.
 * Replace HOLD_FOR_SHOT with nonblocking shooter/transfer states before enabling this OpMode.
 */
@Disabled
@Autonomous(name = "TEMPLATE Hive State Machine Auto", group = "Templates")
public final class HiveStateMachineAutoTemplate
        extends StateMachineAutoBase<HiveStateMachineAutoTemplate.State> {

    enum State {
        PLAN_TO_SHOT,
        FOLLOW_TO_SHOT,
        HOLD_FOR_SHOT,
        DONE,
        FAILED,
        EMERGENCY_STOP
    }

    private MecanumDrive drive;
    private DynamicPurePursuitFollower follower;
    private FieldConfig field;
    private GoalSelector goalSelector;
    private List<Point> pendingPath;
    private double pendingFinalHeading;

    @Override
    protected void onRobotInit() {
        drive = new MecanumDrive(
                hardwareMap, initialPose(), AutomationConfig.mecanumLocalizerMode());
        follower = new DynamicPurePursuitFollower(drive);
        field = HiveAssistConfig.fieldConfig();
        goalSelector = new GoalSelector(new AStarPlanner(new FieldMap(field), field.gridIn));
    }

    @Override
    protected void onRobotInitLoop() {
        drive.updatePoseEstimate();
        telemetry.addData("Template safety lock", HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED);
        telemetry.addLine("This template stays disabled until paths and mechanisms are tested.");
    }

    @Override
    protected State initialState() {
        return State.PLAN_TO_SHOT;
    }

    @Override
    protected State failureState() {
        return State.FAILED;
    }

    @Override
    protected State emergencyState() {
        return State.EMERGENCY_STOP;
    }

    @Override
    protected long emergencyReserveMillis() {
        return 2_500;
    }

    @Override
    protected void onStateEnter(State state) {
        if (state == State.FOLLOW_TO_SHOT) {
            follower.start(pendingPath, pendingFinalHeading);
        }
    }

    @Override
    protected void runState(State state) {
        switch (state) {
            case PLAN_TO_SHOT:
                planToBlueAudienceShot();
                break;

            case FOLLOW_TO_SHOT:
                follower.update();
                if (follower.isFinished()) {
                    if ("shooting position reached".equals(follower.status())) {
                        transitionTo(State.HOLD_FOR_SHOT, "shooting pose reached");
                    } else {
                        fail(follower.status());
                    }
                } else if (stateTimedOut(8_000)) {
                    fail("follow state timed out");
                }
                break;

            case HOLD_FOR_SHOT:
                stopRobotMotion();
                // Add nonblocking shooter/transfer updates here. Never call sleep().
                if (stateElapsedMillis() >= 750) {
                    transitionTo(State.DONE, "shot-state placeholder complete");
                }
                break;

            case DONE:
            case FAILED:
            case EMERGENCY_STOP:
                stopRobotMotion();
                break;
        }
    }

    private void planToBlueAudienceShot() {
        try {
            field.requireVerifiedForRobot();
            List<Point> goals = HiveAssistConfig.goals()
                    .forTarget(Alliance.BLUE, FieldSide.AUDIENCE);
            List<Point> path = goalSelector.bestPath(currentFieldPosition(), goals);
            if (path.size() < 2) {
                fail("no safe shooting path");
                return;
            }

            Point goal = path.get(path.size() - 1);
            pendingFinalHeading = Math.atan2(
                    HiveAssistConfig.HIVE_CENTER_Y_IN - goal.y,
                    HiveAssistConfig.BLUE_HIVE_X_IN - goal.x);
            pendingPath = path;
            transitionTo(State.FOLLOW_TO_SHOT, "safe path generated");
        } catch (IllegalStateException error) {
            fail(error.getMessage());
        }
    }

    private Point currentFieldPosition() {
        Pose2d pose = drive.localizer.getPose();
        return new Point(
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN);
    }

    @Override
    protected void stopRobotMotion() {
        if (follower != null) follower.cancel();
    }

    @Override
    protected void addStateTelemetry() {
        if (follower != null) telemetry.addData("Follower", follower.status());
    }

    private static Pose2d initialPose() {
        return new Pose2d(
                HiveAssistConfig.START_FIELD_X_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                HiveAssistConfig.START_FIELD_Y_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toRadians(HiveAssistConfig.START_HEADING_DEG));
    }
}
