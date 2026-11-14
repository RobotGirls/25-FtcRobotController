package org.firstinspires.ftc.teamcode.biobuzz2627.teleop;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.PinpointLocalizer;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.DynamicPurePursuitFollower;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveAssistConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveDriveAssistRobot;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.HiveTargetSelection;
import org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist.ManualMecanumDriver;
import org.ftc.hiveplanner.AStarPlanner;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.DriveAssist;
import org.ftc.hiveplanner.FieldConfig;
import org.ftc.hiveplanner.FieldMap;
import org.ftc.hiveplanner.FieldSide;
import org.ftc.hiveplanner.GoalSelector;

/**
 * Issue 40: normal mecanum TeleOp plus hold-A Pure Pursuit navigation to a selected Hive side.
 *
 * <p>Driver 1 controls:</p>
 * <ul>
 *   <li>INIT only: X selects BLUE, B selects RED.</li>
 *   <li>Left bumper selects the alliance/audience-side Hive shooting position.</li>
 *   <li>Right bumper selects the far-side Hive shooting position.</li>
 *   <li>Hold A to generate a path from the current Pinpoint pose and follow it.</li>
 *   <li>Release A to cancel and command zero drive power immediately.</li>
 *   <li>Sticks drive normally while A is not held. Stick movement during assist cancels it.</li>
 * </ul>
 *
 * <p>This OpMode controls only drivetrain motors and Pinpoint. It does not initialize Limelight,
 * shooter, intake, turret, or servos.</p>
 */
@TeleOp(name = "Issue 40 - Hold A Hive Assist", group = "Pure Pursuit")
public final class PurePursuitHiveAssistTeleOp extends LinearOpMode {
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

        // Pinpoint is relative localization. X/B seeds its field pose before START.
        while (opModeInInit()) {
            drive.updatePoseEstimate();
            if (selection.updateAlliance(gamepad1, driveAssist)) {
                drive.localizer.setPose(startingPose(selection.alliance()));
            }
            selection.updateSideFromBumpers(gamepad1);
            showTelemetry(drive, pathFollower, selection, driveAssist, "", true);
            sleep(20);
        }
        if (isStopRequested()) return;

        boolean previousAHeld = false;
        String assistError = "";
        try {
            while (opModeIsActive()) {
                drive.updatePoseEstimate();
                selection.updateSideFromBumpers(gamepad1);

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

                // A owns the drivetrain while held. On release, deliberately command zero for
                // this loop before returning normal stick control on the following loop.
                if (!driveAssist.active()) {
                    if (aHeld || aJustReleased) {
                        manualDriver.stop();
                    } else {
                        manualDriver.drive(gamepad1);
                    }
                }

                showTelemetry(drive, pathFollower, selection, driveAssist,
                        assistError, false);
                idle();
            }
        } finally {
            robot.cancelFollowerAndStop();
            manualDriver.stop();
        }
    }

    private void showTelemetry(MecanumDrive drive,
                               DynamicPurePursuitFollower follower,
                               HiveTargetSelection selection,
                               DriveAssist assist,
                               String assistError,
                               boolean inInit) {
        Pose2d pose = drive.localizer.getPose();
        boolean pinpointReady = drive.localizer instanceof PinpointLocalizer
                && ((PinpointLocalizer) drive.localizer).isReady();

        telemetry.addData("Alliance", "%s%s", selection.alliance(),
                inInit ? "  (X blue / B red)" : "  (locked after START)");
        telemetry.addData("Destination", "%s  (LB alliance side / RB far side)",
                selection.side() == FieldSide.AUDIENCE ? "ALLIANCE SIDE" : "FAR SIDE");
        telemetry.addData("A button", gamepad1.a ? "HELD — assist owns drive" : "released");
        telemetry.addData("Pinpoint", pinpointReady ? "READY" : "NOT READY — assist disabled");
        telemetry.addData("Field pose", "x %.1f, y %.1f, h %.1f°",
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                Math.toDegrees(pose.heading.toDouble()));
        telemetry.addData("Assist", assist.active() ? follower.status()
                : "select bumper, then hold A");
        telemetry.addData("Generated waypoints", follower.path().size());

        if (!HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED) {
            telemetry.addLine("LOCKED: verify Hive geometry, robot radius, start poses, and goals");
            telemetry.addLine("Then set HiveAssistConfig.GEOMETRY_AND_GOALS_VERIFIED = true");
        }
        if (!assistError.isEmpty()) telemetry.addData("Assist blocked", assistError);
        if (inInit) {
            telemetry.addLine("Place robot at the configured start pose before pressing START");
        }
        telemetry.update();
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
