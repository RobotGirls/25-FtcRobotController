package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.ftc.hiveplanner.Alliance;
import org.ftc.hiveplanner.DriveAssist;
import org.ftc.hiveplanner.Point;

import java.util.List;

/** FTC/Road Runner bridge for the hardware-independent DriveAssist state machine. */
public final class HiveDriveAssistRobot implements DriveAssist.Robot {
    private final MecanumDrive drive;
    private final DynamicPurePursuitFollower follower;
    private final ManualMecanumDriver manualDriver;
    private final Gamepad gamepad;
    private final HiveTargetSelection selection;

    public HiveDriveAssistRobot(MecanumDrive drive, DynamicPurePursuitFollower follower,
                                ManualMecanumDriver manualDriver, Gamepad gamepad,
                                HiveTargetSelection selection) {
        this.drive = drive;
        this.follower = follower;
        this.manualDriver = manualDriver;
        this.gamepad = gamepad;
        this.selection = selection;
    }

    @Override
    public Point positionInches() {
        Pose2d pose = drive.localizer.getPose();
        return new Point(
                pose.position.x + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN,
                pose.position.y + HiveAssistConfig.ROAD_RUNNER_FIELD_OFFSET_IN);
    }

    @Override
    public boolean localizationValid() {
        Pose2d pose = drive.localizer.getPose();
        return Double.isFinite(pose.position.x) && Double.isFinite(pose.position.y)
                && Double.isFinite(pose.heading.toDouble());
    }

    @Override
    public boolean manualOverrideRequested() {
        return manualDriver.overrideRequested(gamepad);
    }

    @Override
    public void followPath(List<Point> waypoints) {
        Point goal = waypoints.get(waypoints.size() - 1);
        double hiveX = selection.alliance() == Alliance.RED
                ? HiveAssistConfig.RED_HIVE_X_IN : HiveAssistConfig.BLUE_HIVE_X_IN;
        double heading = Math.atan2(
                HiveAssistConfig.HIVE_CENTER_Y_IN - goal.y,
                hiveX - goal.x);
        follower.start(waypoints, heading);
    }

    @Override
    public void updateFollower() {
        follower.update();
    }

    @Override
    public boolean followerFinished() {
        return follower.isFinished();
    }

    @Override
    public void cancelFollowerAndStop() {
        follower.cancel();
    }
}
