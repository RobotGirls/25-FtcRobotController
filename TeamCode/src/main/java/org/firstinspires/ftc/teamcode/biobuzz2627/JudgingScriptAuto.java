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
    public DcMotor leftFront = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public DcMotor leftBack = null;

    public ElapsedTime timer;

    public RevBlinkinLedDriver ledLights;

  // Add mech code here

    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        ledLights = hardwareMap.get(RevBlinkinLedDriver.class, "ledLights");

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


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
                turnRight(leftFront, rightFront, leftBack, rightBack);
            } else if (timer.seconds() > 7) {
                turnLeft(leftFront, rightFront, leftBack, rightBack);
            } else if (timer.seconds() > 9) {
                ledLights(ledLights, "purple");
                driveBack(leftFront, rightFront, leftBack, rightBack);
            } else if (timer.seconds() > 11) {
                driveForward(leftFront, rightFront, leftBack, rightBack);
            } else {
                leftFront.setPower(0);
                leftFront.setPower(0);
                rightBack.setPower(0);
                rightFront.setPower(0);
            }

        }

        // Pace this loop so jaw action is reasonable speed.
        sleep(50);

    }

    public void turnRight(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(1);
        leftBack.setPower(1);
        rightBack.setPower(-1);
        rightFront.setPower(-1);
    }
    public void turnLeft(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(-1);
        leftBack.setPower(-1);
        rightBack.setPower(1);
        rightFront.setPower(1);
    }
    public void driveForward(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(1);
        leftBack.setPower(1);
        rightBack.setPower(1);
        rightFront.setPower(1);
    }
    public void driveBack(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(-1);
        leftBack.setPower(-1);
        rightBack.setPower(-1);
        rightFront.setPower(-1);
    }

    public void ledLights(RevBlinkinLedDriver blinkin, String color) {
        if (Objects.equals(color, "purple")) {
            blinkin.setPattern(RevBlinkinLedDriver.BlinkinPattern.VIOLET);
        } else if (Objects.equals(color, "sparkle")) {
            blinkin.setPattern(RevBlinkinLedDriver.BlinkinPattern.CP1_2_TWINKLES);

        }

    }
}