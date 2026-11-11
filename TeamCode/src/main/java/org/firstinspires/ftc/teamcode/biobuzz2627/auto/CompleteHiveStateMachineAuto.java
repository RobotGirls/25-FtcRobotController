package org.firstinspires.ftc.teamcode.biobuzz2627.auto;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.logging.AutoEventLogger;
import org.firstinspires.ftc.teamcode.biobuzz2627.navigation.HiveAdaptivePlanner;
import org.firstinspires.ftc.teamcode.biobuzz2627.navigation.ReplanningPathController;
import org.firstinspires.ftc.teamcode.biobuzz2627.robot.BioBuzzRobot;
import org.firstinspires.ftc.teamcode.biobuzz2627.statemachine.StateMachineAutoBase;
import org.firstinspires.ftc.teamcode.biobuzz2627.strategy.AutoStrategy;
import org.firstinspires.ftc.teamcode.biobuzz2627.strategy.AutoStrategySelector;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.FieldMap;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.Point;

/**
 * Complete nonblocking integration example. It stays disabled until every verification flag,
 * sensor, mechanism direction, Limelight transform, goal, and emergency park route is tested.
 */
@Disabled
@Autonomous(name = "LOCKED Complete Hive State Auto", group = "Templates")
public final class CompleteHiveStateMachineAuto
        extends StateMachineAutoBase<CompleteHiveStateMachineAuto.State> {

    enum State {
        VERIFY_CONFIGURATION,
        PLAN_TO_SHOT,
        FOLLOW_TO_SHOT,
        SHOOT,
        PLAN_TO_PARK,
        FOLLOW_TO_PARK,
        EMERGENCY_PARK,
        DONE,
        FAILED
    }

    private final AutoStrategySelector selector = new AutoStrategySelector();
    private final AutoEventLogger eventLogger = new AutoEventLogger();
    private BioBuzzRobot robot;
    private ReplanningPathController navigation;
    private AutoStrategy strategy;
    private boolean shooterStarted;
    private boolean emergencyPathStarted;
    private Pose2d shotFollowStart;

    @Override
    protected void onRobotInit() {
        AutoStrategy initial = new AutoStrategy(
                Alliance.BLUE, FieldSide.AUDIENCE, new Point(18, 18));
        robot = new BioBuzzRobot(hardwareMap, initialPose(), initial);
        FieldConfig field = HiveAssistConfig.fieldConfig();
        navigation = new ReplanningPathController(robot.drive,
                new HiveAdaptivePlanner(new AStarPlanner(new FieldMap(field), field.gridIn)));
        robot.start();
    }

    @Override
    protected void onRobotInitLoop() {
        if (selector.update(gamepad1)) {
            robot.targeting.setTarget(selector.alliance(), selector.side());
        }
        robot.drive.updatePoseEstimate();
        robot.limelight.update(Math.toDegrees(
                robot.drive.localizer.getPose().heading.toDouble()));
        robot.targeting.update(robot.limelight.latest());
        telemetry.addData("Alliance", selector.alliance());
        telemetry.addData("Shooting/park side", selector.side());
        telemetry.addData("Selected tag", robot.targeting.description());
        telemetry.addData("Robot health", robot.health.summary());
        telemetry.addData("Complete auto lock", AutomationConfig.COMPLETE_AUTO_VERIFIED);
    }

    @Override
    protected void onAutoStart() {
        strategy = selector.freeze();
        robot.targeting.setTarget(strategy.alliance, strategy.shootingSide);
    }

    @Override protected State initialState() { return State.VERIFY_CONFIGURATION; }
    @Override protected State failureState() { return State.EMERGENCY_PARK; }
    @Override protected State emergencyState() { return State.EMERGENCY_PARK; }
    @Override protected long emergencyReserveMillis() {
        return AutomationConfig.EMERGENCY_PARK_RESERVE_MS;
    }

    @Override
    protected void onStateEnter(State state) {
        eventLogger.entered(state, transitionReason(), robot.drive.localizer.getPose(),
                robot.mechanismStatus());
        if (state == State.FOLLOW_TO_SHOT) {
            double hiveX = strategy.alliance == Alliance.RED
                    ? HiveAssistConfig.RED_HIVE_X_IN : HiveAssistConfig.BLUE_HIVE_X_IN;
            if (!navigation.startBestFacing(
                    HiveAssistConfig.goals().forTarget(strategy.alliance, strategy.shootingSide),
                    hiveX, HiveAssistConfig.HIVE_CENTER_Y_IN)) {
                fail(navigation.status());
                return;
            }
            shooterStarted = false;
            shotFollowStart = robot.drive.localizer.getPose();
        }
        if (state == State.FOLLOW_TO_PARK
                && !navigation.startTo(strategy.parkGoal,
                Math.toRadians(AutomationConfig.PARK_HEADING_DEG))) {
            transitionTo(State.FAILED, "normal park path unavailable");
            return;
        }
        if (state == State.SHOOT) robot.shot.requestShot();
        if (state == State.EMERGENCY_PARK) {
            navigation.cancel("emergency park requested");
            robot.intake.stop();
            robot.shot.stop();
            emergencyPathStarted = false;
        }
    }

    @Override
    protected void runState(State state) {
        robot.updateVisionAndMechanisms();
        if (!navigation.active()) robot.drive.updatePoseEstimate();
        if (robot.updatePoseFromLimelight()) navigation.requestReplan();

        if (robot.mechanismFaulted()
                && state != State.EMERGENCY_PARK && state != State.FAILED) {
            fail(robot.mechanismStatus());
            return;
        }

        switch (state) {
            case VERIFY_CONFIGURATION:
                verifyConfiguration();
                break;
            case PLAN_TO_SHOT:
                planToShot();
                break;
            case FOLLOW_TO_SHOT:
                followToShot();
                break;
            case SHOOT:
                updateShot();
                break;
            case PLAN_TO_PARK:
                planToPark();
                break;
            case FOLLOW_TO_PARK:
                followToPark();
                break;
            case EMERGENCY_PARK:
                emergencyPark();
                break;
            case DONE:
            case FAILED:
                stopRobotMotion();
                break;
        }
    }

    private void verifyConfiguration() {
        try {
            HiveAssistConfig.fieldConfig().requireVerifiedForRobot();
            AutomationConfig.requireMechanismsVerified();
            if (!robot.health.completeAutoReady()) {
                throw new IllegalStateException(robot.health.summary());
            }
            transitionTo(State.PLAN_TO_SHOT, "configuration verified");
        } catch (IllegalStateException error) {
            transitionTo(State.FAILED, error.getMessage());
        }
    }

    private void planToShot() {
        transitionTo(State.FOLLOW_TO_SHOT, "generate shooting path on state entry");
    }

    private void followToShot() {
        navigation.update();
        // Start flywheel only after drivetrain movement has begun; it then updates concurrently.
        Pose2d pose = robot.drive.localizer.getPose();
        double moved = Math.hypot(
                pose.position.x - shotFollowStart.position.x,
                pose.position.y - shotFollowStart.position.y);
        if (!shooterStarted && moved >= 1.0) {
            robot.shooter.spinUp();
            shooterStarted = true;
        }
        if (navigation.finished()) {
            if (navigation.succeeded()) transitionTo(State.SHOOT, "shooting pose reached");
            else fail(navigation.status());
        }
    }

    private void updateShot() {
        if (robot.shot.complete()) {
            robot.shot.stop();
            transitionTo(State.PLAN_TO_PARK, "shot complete");
        } else if (stateTimedOut(5000)) {
            fail("shooting sequence timed out: " + robot.shot.status());
        }
    }

    private void planToPark() {
        transitionTo(State.FOLLOW_TO_PARK, "generate park path on state entry");
    }

    private void followToPark() {
        navigation.update();
        if (navigation.finished()) {
            transitionTo(navigation.succeeded() ? State.DONE : State.FAILED,
                    navigation.status());
        }
    }

    private void emergencyPark() {
        if (!AutomationConfig.PARK_GOALS_VERIFIED) {
            transitionTo(State.FAILED, "emergency park goal unverified");
            return;
        }
        if (!emergencyPathStarted) {
            emergencyPathStarted = true;
            if (!navigation.startTo(strategy.parkGoal,
                    Math.toRadians(AutomationConfig.PARK_HEADING_DEG))) {
                transitionTo(State.FAILED, "emergency park path unavailable");
                return;
            }
        }
        navigation.update();
        if (navigation.finished()) {
            transitionTo(navigation.succeeded() ? State.DONE : State.FAILED,
                    "emergency " + navigation.status());
        }
    }

    @Override
    protected void stopRobotMotion() {
        if (robot != null) robot.drive.setDrivePowers(
                new PoseVelocity2d(new Vector2d(0, 0), 0));
        if (robot != null) {
            robot.intake.stop();
            robot.shot.stop();
        }
    }

    @Override
    protected void onAutoStop() {
        if (navigation != null) navigation.cancel("OpMode stopped");
        if (robot != null) robot.stopAll();
    }

    @Override
    protected void addStateTelemetry() {
        telemetry.addData("Navigation", navigation.status());
        telemetry.addData("Mechanisms", robot.mechanismStatus());
        telemetry.addData("Limelight pose", robot.poseCorrector.status());
        telemetry.addData("Range", robot.targeting.description());
    }

    private static Pose2d initialPose() {
        return new Pose2d(
                HiveAssistConfig.START_FIELD_X_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                HiveAssistConfig.START_FIELD_Y_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toRadians(HiveAssistConfig.START_HEADING_DEG));
    }
}
