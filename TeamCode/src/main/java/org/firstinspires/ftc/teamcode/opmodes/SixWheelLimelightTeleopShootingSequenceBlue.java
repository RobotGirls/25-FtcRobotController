package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@TeleOp(name = "ILT TELEOP SEQUENCE BLUE")

public class SixWheelLimelightTeleopShootingSequenceBlue extends LinearOpMode {

    private final int ALIGN_THRESHOLD = 3;
    Servo hoodServo;

    public ElapsedTime sequenceTimer;
    private double lastError = 0;
    private double derivative = 0;
    private double integralSum = 0;

    private double Kp = 0.0165; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power  //turret aiming
    private double Ki = 0; //turret aiming
    private double Kd = 0; //turet aiming
    private double shooterSpeed = -1600; // default speed: far LZ

    Limelight3A limelight;
    DcMotorEx shooter;
    DcMotor turret;
    DcMotor intake;
    DcMotor transfer;

    public DcMotor  leftFront  = null;
    public DcMotor leftBack = null;
    public DcMotor rightFront = null;
    public DcMotor  rightBack  = null;
    public boolean shootingOn = false;


    public final double TURRET_OFFSET = 0; // FIXME figure out how many degrees to the side the turret will be aiming relative to front of robot

    @Override
    public void runOpMode() throws InterruptedException {

        sequenceTimer = new ElapsedTime();

        telemetry.setMsTransmissionInterval(11);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

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
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        intake = hardwareMap.get(DcMotor.class,"intake");
        transfer = hardwareMap.get(DcMotor.class,"transfer");
        hoodServo = hardwareMap.get(Servo.class, "shooterHood");

        transfer.setDirection(DcMotorSimple.Direction.REVERSE);

        shooter.setVelocityPIDFCoefficients(10,3,3,2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.setMsTransmissionInterval(11);

        limelight.pipelineSwitch(1);

        /*
         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
         */
        limelight.start();

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
                sequenceTimer.reset();
                sequenceTimer.startTime();

                shootingOn = true;
            }

            if (shootingOn) {
                if (sequenceTimer.milliseconds() < 5000) {

                    telemetry.addData("Sequence Status:", "Shooter speeds up and transfer outtakes");
                    telemetry.addData("Timer Status:", sequenceTimer.milliseconds());
                    shooter.setVelocity(shooterSpeed);
                    //transfer.setPower(1); // transfer out
                } else if (sequenceTimer.milliseconds() < 10000) {

                    telemetry.addData("Sequence Status:","Run intake + transfer, shoot ball") ;
                    telemetry.addData("Timer Status:", sequenceTimer.milliseconds());
                    shooter.setVelocity(shooterSpeed);
                    transfer.setPower(-1);
                    intake.setPower(1);
                }
                else if (sequenceTimer.milliseconds() > 10000 && sequenceTimer.milliseconds() < 11000){
                    shooter.setVelocity(0);
                    transfer.setPower(0);
                    intake.setPower(0);
                    shootingOn = false;
                }
            }
            else {
                if (gamepad2.left_bumper) {
                    // INTAKE
                    intake.setPower(-1);
                }
                else if (gamepad2.right_bumper) {
                    transfer.setPower(1);
                    intake.setPower(1);
                } else {
                    transfer.setPower(0);
                    intake.setPower(0);
                }
            }


            LLResult result = limelight.getLatestResult();

            if (result.isValid()) {
                LLResult result1 = limelight.getLatestResult();
/*
                telemetry.addData("tx", result1.getTx());
                telemetry.addData("txnc", result1.getTxNC());
                telemetry.addData("ty", result1.getTy());
                telemetry.addData("tync", result1.getTyNC());

 */
                Pose3D botpose = result1.getBotpose();
                double robotx = botpose.getPosition().x;
                double roboty = botpose.getPosition().y;
                telemetry.addData("MT1 Location", "(" + robotx + ", " + roboty + ")");
                if (robotx < -0.5) {
                    // if robot is very close to the goal
                    shooterSpeed = -1250;
                    hoodServo.setPosition(0.45);
                } else if (robotx >= -0.5 && robotx < 0.2) {
                    // if robot is around the tip (farthest end) of the close launch zone
                    shooterSpeed = -1340;
                    hoodServo.setPosition(0.25); // FIXME change servo values to those found in testing

                } else {
                    // if robot is in the far launch zone
                    shooterSpeed = -1640;
                    hoodServo.setPosition(0.1);
                }

                double error = result1.getTx();
                ElapsedTime timer = new ElapsedTime();
                if (Math.abs(error) > ALIGN_THRESHOLD) {
                    error = -1 * result1.getTx();
                    derivative = (error - lastError) / timer.seconds();
                    integralSum = integralSum + (error * timer.seconds());
                    power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                    turret.setPower(power);
                    lastError = error;
                } else {
                    turret.setPower(0);  // aligned
                }

            } else {
                // if we don't see an apriltag (likely because too close to goal)
                telemetry.addData("Limelight", "No data available");

                shooterSpeed = -1250;
                double turretPower = gamepad2.left_stick_x;

                turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                if (turret.getCurrentPosition() >= -2000 && turret.getCurrentPosition() <= 2000) { // FIXME change encoder value after testing
                    turret.setPower(turretPower);
                    //telemetry.addData("Current Motor Position", turret.getCurrentPosition());
                } else {
                    turret.setPower(0);
                    //telemetry.addData("Current Motor Position", "Too Far!");
                }

            }

            telemetry.addData("Flywheel Velocity", shooter.getVelocity());
            telemetry.addData("Turret Error",lastError);
            telemetry.addData("Flywheel Speed",shooterSpeed);
            telemetry.addData("Hood Height",hoodServo.getPosition());
            telemetry.update();

        }
        limelight.stop();
    }

}