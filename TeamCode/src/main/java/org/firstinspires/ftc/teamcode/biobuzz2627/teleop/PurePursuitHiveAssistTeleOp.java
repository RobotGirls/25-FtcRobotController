package org.firstinspires.ftc.teamcode.biobuzz2627.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.DynamicPurePursuitFollower;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveDriveAssistRobot;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveTargetSelection;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.LimelightHiveTargetTracker;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.LimelightTurretAimer;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.ManualMecanumDriver;
import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.DriveAssist;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.FieldMap;
import org.ftc.hiveplanner.GoalSelector;

/**
 * TeleOp with normal mecanum control, hold-to-run Hive path assist, and Limelight turret aiming.
 * This OpMode deliberately does not initialize or command shooter/intake/transfer hardware.
 *
 * <p>Driver 1 controls:</p>
 * <ul>
 *   <li>Sticks: normal mecanum drive; stick movement cancels assist</li>
 *   <li>X / B: select BLUE / RED tag filter</li>
 *   <li>D-pad left / right: select AUDIENCE / FAR side</li>
 *   <li>Hold left bumper: generate/follow a path and aim the turret when the tag is visible</li>
 *   <li>Release left bumper or press right bumper: cancel</li>
 * </ul>
 */
@TeleOp(name = "TEST PP Hive Shooting Assist", group = "Pure Pursuit")
public final class PurePursuitHiveAssistTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        // ----- Drivetrain and path follower -----
        MecanumDrive drive = new MecanumDrive(
                hardwareMap, initialPose(), AutomationConfig.mecanumLocalizerMode());
        ManualMecanumDriver manualDriver = new ManualMecanumDriver(drive);
        DynamicPurePursuitFollower pathFollower = new DynamicPurePursuitFollower(drive);

        // ----- Driver target selection and Limelight filter -----
        HiveTargetSelection targetSelection = new HiveTargetSelection();
        LimelightHiveTargetTracker targetTracker = new LimelightHiveTargetTracker(
                hardwareMap.get(Limelight3A.class, "limelight"));
        LimelightTurretAimer turretAimer = new LimelightTurretAimer(
                hardwareMap.get(DcMotor.class, "turret"));

        // ----- Hardware-independent path-planning state machine -----
        FieldConfig field = HiveAssistConfig.fieldConfig();
        GoalSelector goalSelector = new GoalSelector(
                new AStarPlanner(new FieldMap(field), field.gridIn));
        HiveDriveAssistRobot robot = new HiveDriveAssistRobot(
                drive, pathFollower, manualDriver, gamepad1, targetSelection);
        DriveAssist driveAssist = new DriveAssist(
                robot, goalSelector, field, HiveAssistConfig.goals(), targetSelection.alliance());
        driveAssist.setSide(targetSelection.side());

        targetTracker.start();
        try {
            // Selection and camera feedback remain live while waiting for START.
            while (opModeInInit()) {
                updateTargetSelection(targetSelection, targetTracker, driveAssist);
                targetTracker.update(targetSelection);
                turretAimer.update(false, targetTracker);
                showTelemetry(drive, pathFollower, targetSelection, targetTracker,
                        turretAimer, driveAssist, "");
                sleep(20);
            }
            if (isStopRequested()) return;

            String assistError = "";
            while (opModeIsActive()) {
                updateTargetSelection(targetSelection, targetTracker, driveAssist);
                targetTracker.update(targetSelection);

                // Path generation uses Road Runner localization and surveyed field goals. It must
                // work from across the field even when the selected AprilTag is not visible yet.
                boolean assistHeld = gamepad1.left_bumper;
                try {
                    driveAssist.update(assistHeld, gamepad1.right_bumper);
                    assistError = "";
                } catch (IllegalStateException error) {
                    robot.cancelFollowerAndStop();
                    assistError = error.getMessage();
                }

                // Limelight has a separate job: aim the turret whenever the selected tag enters
                // view. Tag loss stops the turret, but never cancels an active drivetrain path.
                turretAimer.update(assistHeld && !gamepad1.right_bumper, targetTracker);

                // This is the normal driving path. It runs whenever pure pursuit is inactive.
                if (!driveAssist.active()) {
                    manualDriver.drive(gamepad1);
                }

                showTelemetry(drive, pathFollower, targetSelection, targetTracker,
                        turretAimer, driveAssist, assistError);
                idle();
            }
        } finally {
            robot.cancelFollowerAndStop();
            turretAimer.stop();
            targetTracker.stop();
        }
    }

    private void updateTargetSelection(HiveTargetSelection selection,
                                       LimelightHiveTargetTracker tracker,
                                       DriveAssist assist) {
        if (selection.update(gamepad1, assist)) {
            tracker.invalidate();
        }
    }

    private void showTelemetry(MecanumDrive drive,
                               DynamicPurePursuitFollower follower,
                               HiveTargetSelection selection,
                               LimelightHiveTargetTracker tracker,
                               LimelightTurretAimer turretAimer,
                               DriveAssist assist,
                               String assistError) {
        Pose2d pose = drive.localizer.getPose();
        telemetry.addData("Color filter", "%s  (X blue / B red)", selection.alliance());
        telemetry.addData("HIVE side", "%s  (d-pad left audience / right far)", selection.side());
        telemetry.addData("Selected cluster", selection.clusterName());
        telemetry.addData("Limelight target", tracker.targetReady()
                ? "ready — tag " + tracker.lastTagId() : "not visible");
        telemetry.addData("FieldViewer camera", tracker.fieldViewerRelayStatus());
        telemetry.addData("Turret aim", "%s  error %.2f°  power %.2f",
                turretAimer.status(), turretAimer.errorDegrees(), turretAimer.commandedPower());
        if (!HiveAssistConfig.TURRET_AIMING_VERIFIED) {
            telemetry.addLine("TURRET LOCKED: verify motor direction/limits, then enable in Dashboard");
        }
        addShootingRangeTelemetry(tracker);
        telemetry.addData("Assist", assist.active() ? follower.status()
                : "hold LEFT BUMPER; release/sticks cancel");
        telemetry.addData("Verified", HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED);
        if (!HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED) {
            telemetry.addLine("DRIVE ASSIST LOCKED: survey/test coordinates, then enable in Dashboard");
        }
        if (!assistError.isEmpty()) telemetry.addData("Assist blocked", assistError);
        telemetry.addData("Field pose", "x %.1f, y %.1f, h %.1f°",
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toDegrees(pose.heading.toDouble()));
        telemetry.update();
    }

    private void addShootingRangeTelemetry(LimelightHiveTargetTracker tracker) {
        LimelightShootingRange.Measurement range = tracker.latestRange();
        if (range == null) {
            telemetry.addData("Shooting range", "no selected-tag measurement");
            return;
        }

        telemetry.addData("Camera -> tag", "%.3f m  (x %.3f, y %.3f, z %.3f)",
                range.distanceMeters(), range.xMeters(), range.yMeters(), range.zMeters());
        telemetry.addData("Tag angles", "tx %.2f°, ty %.2f°",
                range.txDegrees(), range.tyDegrees());
        if (!HiveAssistConfig.SHOOTING_RANGE_LIMITS_VERIFIED) {
            telemetry.addData("Shooting range", "UNCALIBRATED — tune and verify limits");
        } else {
            telemetry.addData("Shooting range",
                    tracker.inVerifiedShootingRange() ? "READY TO SHOOT" : "MOVE ROBOT");
        }
    }

    private static Pose2d initialPose() {
        return new Pose2d(
                HiveAssistConfig.START_FIELD_X_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                HiveAssistConfig.START_FIELD_Y_IN - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toRadians(HiveAssistConfig.START_HEADING_DEG));
    }
}
