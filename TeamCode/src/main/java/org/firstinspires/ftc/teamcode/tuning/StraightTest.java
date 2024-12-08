package org.firstinspires.ftc.teamcode.tuning;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name = "Straight test")
public class StraightTest extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public DcMotor lift;
    public CRServo claw;
    //public Servo rotateClaw;
    public DcMotor liftPivot;

// ... other imports and class declaration

    ElapsedTime runtime = new ElapsedTime();


    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);

        //RNRRMecanumDrive drive = new RNRRMecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
        //drive.setPoseEstimate(startPose);

        lift = hardwareMap.get(DcMotor.class, "lift");
        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");
        claw = hardwareMap.get(CRServo.class, "claw");
        //rotateClaw = hardwareMap.servo.get("rotateClaw");

        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

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
        while (opModeIsActive() && runtime.seconds() < 8) {
            // Your code to execute within the loop
            leftFront.setPower(0.48);
            rightFront.setPower(0.48);
            leftBack.setPower(0.48);
            rightBack.setPower(0.48);

            telemetry.addData("Time Elapsed", runtime.seconds());
            telemetry.update();
        }
    }
}