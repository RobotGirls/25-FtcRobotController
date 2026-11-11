package org.firstinspires.ftc.teamcode.biobuzz2627.hiveassist;

import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.MecanumDrive;

/** Normal driver-controlled mecanum movement used whenever path assist is idle. */
public final class ManualMecanumDriver {
    private final MecanumDrive drive;

    public ManualMecanumDriver(MecanumDrive drive) {
        this.drive = drive;
    }

    public void drive(Gamepad gamepad) {
        drive.setDrivePowers(new PoseVelocity2d(
                new Vector2d(-gamepad.left_stick_y, -gamepad.left_stick_x)
                        .times(HiveAssistConfig.MANUAL_DRIVE_SPEED),
                -gamepad.right_stick_x * HiveAssistConfig.MANUAL_DRIVE_SPEED));
        drive.updatePoseEstimate();
    }

    public boolean overrideRequested(Gamepad gamepad) {
        double deadband = HiveAssistConfig.MANUAL_OVERRIDE_DEADBAND;
        return Math.abs(gamepad.left_stick_x) > deadband
                || Math.abs(gamepad.left_stick_y) > deadband
                || Math.abs(gamepad.right_stick_x) > deadband;
    }
}
