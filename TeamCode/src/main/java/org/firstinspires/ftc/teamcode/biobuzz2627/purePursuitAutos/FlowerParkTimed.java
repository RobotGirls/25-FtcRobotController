package org.firstinspires.ftc.teamcode.biobuzz2627.purePursuitAutos;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Localization-free version of Flower Park.
 *
 * <p>This intentionally behaves like a simple FTC Blocks autonomous: apply fixed wheel powers,
 * wait for a calibrated amount of time, stop, and start the next movement. It does not use Pure
 * Pursuit, Road Runner, wheel odometry, Pinpoint, an IMU, or AprilTags.</p>
 *
 * <p>Place the robot at the same starting pose as FlowerPark: (-12, 69), facing -90 degrees.
 * Tune the public power/time values on a clear practice field before running near the Hive.</p>
 */
@Config
@Autonomous(name = "Flower Park - Timed", group = "No Localization")
public class FlowerParkTimed extends LinearOpMode {
    // These are starting estimates, not measured values for your robot.
    public static double FORWARD_POWER = 0.68;
    public static long FORWARD_TIME_MS = 200;
    public static double STRAFE_POWER = 0.50;
    public static long STRAFE_LEFT_TIME_MS = 2000;
    public static long PAUSE_BETWEEN_MOVES_MS = 200;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    @Override
    public void runOpMode() {
        initializeDrive();

        telemetry.addLine("Flower Park - timed/no localization");
        telemetry.addLine("Start at (-12, 69), facing -90 degrees");
        telemetry.addLine("Test at low power on an open field first");
        telemetry.update();

        waitForStart();
        if (isStopRequested()) {
            return;
        }

        try {
            // Route leg 1: (-12, 69) -> (-12, 60), about 9 inches forward.
            driveFor("1/2: move away from wall", FORWARD_POWER, 0, FORWARD_TIME_MS);

            // Route leg 2: (-12, 60) -> (34, 60), about 46 inches to robot-left.
            driveFor("2/2: strafe into parking zone", 0, -STRAFE_POWER,
                    STRAFE_LEFT_TIME_MS);
        } finally {
            stopDrive();
        }

        telemetry.addLine("Flower Park timed route complete");
        telemetry.update();
    }

    private void initializeDrive() {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        backRight.setDirection(DcMotorSimple.Direction.FORWARD);

        configureMotor(frontLeft);
        configureMotor(frontRight);
        configureMotor(backLeft);
        configureMotor(backRight);
    }

    private void configureMotor(DcMotor motor) {
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setPower(0);
    }

    /** Positive forward drives forward; positive strafe drives robot-right. */
    private void driveFor(String stage, double forward, double strafeRight, long timeMs) {
        double frontLeftPower = forward + strafeRight;
        double backLeftPower = forward - strafeRight;
        double frontRightPower = forward - strafeRight;
        double backRightPower = forward + strafeRight;
        double scale = Math.max(1.0, Math.max(
                Math.max(Math.abs(frontLeftPower), Math.abs(backLeftPower)),
                Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));

        ElapsedTime timer = new ElapsedTime();
        setDrivePower(frontLeftPower / scale, frontRightPower / scale,
                backLeftPower / scale, backRightPower / scale);

        while (opModeIsActive() && timer.milliseconds() < Math.max(0, timeMs)) {
            telemetry.addData("Stage", stage);
            telemetry.addData("Elapsed", "%.0f / %d ms", timer.milliseconds(), timeMs);
            telemetry.update();
            idle();
        }

        stopDrive();
        if (opModeIsActive() && PAUSE_BETWEEN_MOVES_MS > 0) {
            sleep(PAUSE_BETWEEN_MOVES_MS);
        }
    }

    private void setDrivePower(double fl, double fr, double bl, double br) {
        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        backLeft.setPower(bl);
        backRight.setPower(br);
    }

    private void stopDrive() {
        setDrivePower(0, 0, 0, 0);
    }
}
