package org.firstinspires.ftc.teamcode.opmodes;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLStatus;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
@Config
@TeleOp(name = "LM3 SIMPLE TELEOP")

public class SimpleTeleopNoLimelight extends LinearOpMode {

    private final int ALIGN_THRESHOLD = 3;

    private double lastError = 0;

    private double Kp = 0.014; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;
    private double shooterSpeed = -1500;

    DcMotorEx shooter;
    DcMotor turret;
    DcMotor intake;
    DcMotor transfer;

    public DcMotor  leftFront  = null;
    public DcMotor leftBack = null;
    public DcMotor rightFront = null;
    public DcMotor  rightBack  = null;
    public Servo hood = null;

    public static double hoodHeight = 0;

    public final double TURRET_OFFSET = 0; // FIXME figure out how many degrees to the side the turret will be aiming relative to front of robot

    @Override
    public void runOpMode() throws InterruptedException {

        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


//        leftFront.setDirection(DcMotorSimple.Direction.REVERSE); // FIXME reverse the correct left motor
//        rightFront.setDirection(DcMotorSimple.Direction.REVERSE); // FIXME reverse the correct right motor


        turret = hardwareMap.get(DcMotor.class, "turret");
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        intake = hardwareMap.get(DcMotor.class,"intake");
        transfer = hardwareMap.get(DcMotor.class,"transfer");
        hood = hardwareMap.get(Servo.class, "shooterHood");

        shooter.setVelocityPIDFCoefficients(10,3,3,2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.setMsTransmissionInterval(11);

        double power = 0; // initial turret power

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            double drive = gamepad1.left_stick_x; // Remember, Y stick value is reversed; Forward/Backward
            double turn = -gamepad1.right_stick_y; //Left/Right turn

            double leftPower = drive + turn;
            double rightPower = drive - turn;


            leftFront.setPower(leftPower);
            leftBack.setPower(leftPower);
            rightFront.setPower(rightPower);
            rightBack.setPower(rightPower);

            if (gamepad2.x) {
                shooter.setVelocity(-1340); // end of close launch zone

            }
            else if (gamepad2.y) {
                shooter.setVelocity(-1600); // far launch zone
            }
            else {
                shooter.setVelocity(0);
            }
            if (gamepad2.a) {
                hood.setPosition(0);
            }
            else if (gamepad2.b) {
                hood.setPosition(0.8);

            }
            if (gamepad2.left_bumper) {
                transfer.setPower(-1); // FIXME change values accordingly
                intake.setPower(-1); // FIXME change values accordingly
            }
            else if (gamepad2.right_bumper) {
                transfer.setPower(1); // FIXME change values accordingly
                intake.setPower(1); // FIXME change values accordingly
            } else {
                transfer.setPower(0);
                intake.setPower(0);
            }



            telemetry.addData("Flywheel Velocity", shooter.getVelocity());
            telemetry.addData("Flywheel Speed",shooterSpeed);
            telemetry.update();

        }
    }
}