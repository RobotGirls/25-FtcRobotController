package org.firstinspires.ftc.teamcode.biobuzz2627.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.DynamicPurePursuitFollower;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.FieldRelativeTurretAimer;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAimTarget;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveDriveAssistRobot;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveTargetSelection;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.LimelightHiveTargetTracker;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.ManualMecanumDriver;
import org.firstinspires.ftc.teamcode.biobuzz2627.limelight.LimelightShootingRange;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.DriveAssist;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.FieldMap;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.GoalSelector;

/**
 * Issue 41 combines Issue 40 path generation with continuous field-relative turret aiming.
 * Pinpoint and the turret encoder pre-aim at the selected Hive from anywhere on the field;
 * Limelight selected-tag tx performs the final PID refinement when the tag enters view.
 */
@TeleOp(name = "Issue 41 - Pinpoint + LL Turret", group = "Pure Pursuit")
public final class Issue41PinpointTurretTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        HiveTargetSelection selection = new HiveTargetSelection();
        MecanumDrive drive = new MecanumDrive(
                hardwareMap, startingPose(selection.alliance()), MecanumDrive.LocalizerMode.PINPOINT);
        ManualMecanumDriver manualDriver = new ManualMecanumDriver(drive);
        DynamicPurePursuitFollower pathFollower = new DynamicPurePursuitFollower(drive);

        FieldConfig field = HiveAssistConfig.fieldConfig();
        GoalSelector goalSelector = new GoalSelector(
                new AStarPlanner(new FieldMap(field), field.gridIn));
        HiveDriveAssistRobot robot = new HiveDriveAssistRobot(
                drive, pathFollower, manualDriver, gamepad1, selection);
        DriveAssist driveAssist = new DriveAssist(
                robot, goalSelector, field, HiveAssistConfig.goals(), selection.alliance());

        LimelightHiveTargetTracker targetTracker = new LimelightHiveTargetTracker(
                hardwareMap.get(Limelight3A.class, "limelight"));
        FieldRelativeTurretAimer turretAimer = new FieldRelativeTurretAimer(
                hardwareMap.get(DcMotorEx.class, HiveAssistConfig.TURRET_MOTOR_NAME));

        targetTracker.start();
        try {
            // Keep turret power off during INIT. Physically point it robot-forward before INIT;
            // its motor encoder is zeroed by FieldRelativeTurretAimer's constructor.
            while (opModeInInit()) {
                drive.updatePoseEstimate();
                boolean changed = selection.updateAlliance(gamepad1, driveAssist);
                if (changed) {
                    drive.localizer.setPose(startingPose(selection.alliance()));
                    targetTracker.invalidate();
                }
                if (selection.updateSideFromBumpers(gamepad1)) {
                    targetTracker.invalidate();
                }
                targetTracker.update(selection);
                turretAimer.stop();
                showTelemetry(drive, pathFollower, selection, driveAssist,
                        targetTracker, turretAimer, "", true);
                sleep(20);
            }
            if (isStopRequested()) return;

            boolean previousAHeld = false;
            String assistError = "";
            while (opModeIsActive()) {
                drive.updatePoseEstimate();
                if (selection.updateSideFromBumpers(gamepad1)) {
                    targetTracker.invalidate();
                }
                targetTracker.update(selection);

                boolean aHeld = gamepad1.a;
                boolean aJustReleased = previousAHeld && !aHeld;
                previousAHeld = aHeld;
                try {
                    driveAssist.update(aHeld, selection.side());
                    assistError = "";
                } catch (IllegalStateException error) {
                    robot.cancelFollowerAndStop();
                    assistError = error.getMessage();
                }

                if (!driveAssist.active()) {
                    if (aHeld || aJustReleased) {
                        manualDriver.stop();
                    } else {
                        manualDriver.drive(gamepad1);
                    }
                }

                Pose2d pose = drive.localizer.getPose();
                turretAimer.update(pose, pinpointReady(drive), selection, targetTracker);
                showTelemetry(drive, pathFollower, selection, driveAssist,
                        targetTracker, turretAimer, assistError, false);
                idle();
            }
        } finally {
            robot.cancelFollowerAndStop();
            manualDriver.stop();
            turretAimer.stop();
            targetTracker.stop();
        }
    }

    private void showTelemetry(MecanumDrive drive,
                               DynamicPurePursuitFollower follower,
                               HiveTargetSelection selection,
                               DriveAssist assist,
                               LimelightHiveTargetTracker tracker,
                               FieldRelativeTurretAimer turret,
                               String assistError,
                               boolean inInit) {
        Pose2d pose = drive.localizer.getPose();
        HiveAimTarget target = HiveAimTarget.forSelection(selection.alliance(), selection.side());

        telemetry.addData("Alliance", "%s%s", selection.alliance(),
                inInit ? "  (X blue / B red)" : "  (locked after START)");
        telemetry.addData("Hive side", "%s  (LB alliance / RB far)",
                selection.side() == FieldSide.AUDIENCE ? "ALLIANCE" : "FAR");
        telemetry.addData("Aim point", "x %.1f, y %.1f in", target.fieldXIn, target.fieldYIn);
        telemetry.addData("Pinpoint", pinpointReady(drive) ? "READY" : "NOT READY");
        telemetry.addData("Field pose", "x %.1f, y %.1f, h %.1f°",
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toDegrees(pose.heading.toDouble()));
        telemetry.addData("Path assist", assist.active() ? follower.status()
                : "select bumper; hold A to drive");
        if (!assistError.isEmpty()) telemetry.addData("Path blocked", assistError);

        telemetry.addData("Turret mode", turret.mode());
        telemetry.addData("Turret angle", "current %.1f° / desired %.1f°",
                turret.currentTurretDegrees(), turret.desiredTurretDegrees());
        telemetry.addData("Field bearing", "%.1f°", turret.fieldBearingDegrees());
        telemetry.addData("Turret PID", "error %.2f° / power %.2f / trim %.2f°",
                turret.errorDegrees(), turret.commandedPower(), turret.visionTrimDegrees());
        telemetry.addData("Selected tag", tracker.selectedTagVisibleNow()
                ? "VISIBLE — ID " + tracker.lastTagId() : "not visible; Pinpoint pre-aim active");
        LimelightShootingRange.Measurement range = tracker.latestRange();
        if (range != null) {
            telemetry.addData("Limelight", "tx %.2f° / range %.2f m",
                    range.txDegrees(), range.distanceMeters());
        }

        if (!HiveAssistConfig.TURRET_AIMING_VERIFIED) {
            telemetry.addLine("TURRET LOCKED: verify zero, encoder sign, power sign, and gearing");
            telemetry.addLine("Then set HiveAssistConfig.TURRET_AIMING_VERIFIED = true");
        }
        if (!HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED) {
            telemetry.addLine("PATH LOCKED: verify Issue 40 field geometry and goals");
        }
        if (inInit) {
            telemetry.addLine("Before INIT: point turret/shooter/Limelight robot-forward");
        }
        telemetry.update();
    }

    private static boolean pinpointReady(MecanumDrive drive) {
        return drive.localizer instanceof PinpointLocalizer
                && ((PinpointLocalizer) drive.localizer).isReady();
    }

    private static Pose2d startingPose(Alliance alliance) {
        return new Pose2d(
                HiveAssistConfig.startFieldX(alliance)
                        - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                HiveAssistConfig.startFieldY(alliance)
                        - HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toRadians(HiveAssistConfig.startHeadingDegrees(alliance)));
    }
}
