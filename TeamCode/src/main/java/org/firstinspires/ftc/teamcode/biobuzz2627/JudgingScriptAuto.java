package org.firstinspires.ftc.teamcode.biobuzz2627;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Objects;

@Autonomous(name = "Judging Script")
public class JudgingScriptAuto extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor leftFront = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public DcMotor leftBack = null;

    public ElapsedTime timer = new ElapsedTime();

  // Add mech code here

    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

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

        timer.startTime();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            timer.startTime();

            while (timer.seconds() < 5 ) {
                if (timer.seconds() < 2 ) {
                    turnLeft(leftFront, rightFront, rightFront, rightBack);
                } else if (timer.seconds() >= 3 && timer.seconds() < 5) {
                    turnRight(leftFront, rightFront, leftBack, rightBack);
                }
            }

        }

        // Pace this loop so jaw action is reasonable speed.
        sleep(50);

    }

    public void turnRight(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(0.5);
        leftBack.setPower(0.5);
        rightBack.setPower(-0.5);
        rightFront.setPower(-0.5);
    }
    public void turnLeft(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(-0.5);
        leftBack.setPower(-0.5);
        rightBack.setPower(0.5);
        rightFront.setPower(0.5);
    }
    public void stopMoving(DcMotor leftFront, DcMotor rightFront, DcMotor leftBack, DcMotor rightBack) {
        leftFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
        rightFront.setPower(0);
    }


}