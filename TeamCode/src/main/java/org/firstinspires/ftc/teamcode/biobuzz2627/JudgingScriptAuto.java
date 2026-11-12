package org.firstinspires.ftc.teamcode.biobuzz2627;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Objects;

@TeleOp(name = "BIOBUZZ TELEOP")
public class JudgingScriptAuto extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor frontLeft = null;
    public DcMotor frontRight = null;
    public DcMotor backRight = null;
    public DcMotor backLeft = null;

    public ElapsedTime timer;

    public RevBlinkinLedDriver ledLights;

  // Add mech code here

    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        frontLeft = hardwareMap.get(DcMotor.class, "backRight");
        frontRight = hardwareMap.get(DcMotor.class, "frontLeft");
        backRight = hardwareMap.get(DcMotor.class, "backLeft");
        backLeft = hardwareMap.get(DcMotor.class, "frontRight");

        ledLights = hardwareMap.get(RevBlinkinLedDriver.class, "ledLights");

        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips


        // Send telemetry message to signify robot waiting;
        telemetry.addData(">", "Robot Ready.  Press START.");    //
        telemetry.update();

        // Wait for the game to start (driver presses START)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            timer.reset();

            timer.startTime();

            if (timer.seconds() > 5) {
                // Add robot motion
                ledLights(ledLights, "sparkle");
                turnRight(frontLeft, frontRight, backLeft, backRight);
            } else if (timer.seconds() > 7) {
                turnLeft(frontLeft, frontRight, backLeft, backRight);
            } else if (timer.seconds() > 9) {
                ledLights(ledLights, "purple");
                driveBack(frontLeft, frontRight, backLeft, backRight);
            } else if (timer.seconds() > 11) {
                driveForward(frontLeft, frontRight, backLeft, backRight);
            } else {
                frontLeft.setPower(0);
                frontLeft.setPower(0);
                backRight.setPower(0);
                frontRight.setPower(0);
            }

        }

        // Pace this loop so jaw action is reasonable speed.
        sleep(50);

    }

    public void turnRight(DcMotor frontLeft, DcMotor frontRight, DcMotor backLeft, DcMotor backRight) {
        frontLeft.setPower(1);
        backLeft.setPower(1);
        backRight.setPower(-1);
        frontRight.setPower(-1);
    }
    public void turnLeft(DcMotor frontLeft, DcMotor frontRight, DcMotor backLeft, DcMotor backRight) {
        frontLeft.setPower(-1);
        backLeft.setPower(-1);
        backRight.setPower(1);
        frontRight.setPower(1);
    }
    public void driveForward(DcMotor frontLeft, DcMotor frontRight, DcMotor backLeft, DcMotor backRight) {
        frontLeft.setPower(1);
        backLeft.setPower(1);
        backRight.setPower(1);
        frontRight.setPower(1);
    }
    public void driveBack(DcMotor frontLeft, DcMotor frontRight, DcMotor backLeft, DcMotor backRight) {
        frontLeft.setPower(-1);
        backLeft.setPower(-1);
        backRight.setPower(-1);
        frontRight.setPower(-1);
    }

    public void ledLights(RevBlinkinLedDriver blinkin, String color) {
        if (Objects.equals(color, "purple")) {
            blinkin.setPattern(RevBlinkinLedDriver.BlinkinPattern.VIOLET);
        } else if (Objects.equals(color, "sparkle")) {
            blinkin.setPattern(RevBlinkinLedDriver.BlinkinPattern.CP1_2_TWINKLES);

        }

    }
}