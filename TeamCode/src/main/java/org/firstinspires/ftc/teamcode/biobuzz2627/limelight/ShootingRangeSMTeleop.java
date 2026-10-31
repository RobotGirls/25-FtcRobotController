package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "ShootingRangeTeleop Blue")
public class ShootingRangeTeleopBlue extends LinearOpMode {

    public DcMotor leftFront = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public DcMotor leftBack = null;

    public DcMotorEx shooter;

    public DcMotor intake;

    final boolean IS_RED = true;

    boolean validResult;
    boolean validShooting;

    double xValue;

    double distanceToTagCenter;

    private DcMotor turret;


    @Override
    public void runOpMode() {

        leftFront = hardwareMap.get(DcMotor.class, "leftFront");
        rightFront = hardwareMap.get(DcMotor.class, "rightFront");
        rightBack = hardwareMap.get(DcMotor.class, "rightBack");
        leftBack = hardwareMap.get(DcMotor.class, "leftBack");

        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        LimelightSensor limelight = new LimelightSensor(hardwareMap, telemetry, IS_RED);
        Shooter shooter = new Shooter(hardwareMap, telemetry);
        intake = hardwareMap.get(DcMotor.class, "intake");
        turret = hardwareMap.get(DcMotor.class, "turret");


        telemetry.setMsTransmissionInterval(11);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            double yd = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double xd = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(yd) + Math.abs(xd) + Math.abs(rx), 1);
            double frontLeftPower = (yd + xd + rx) / denominator;
            double backLeftPower = (yd - xd + rx) / denominator;
            double frontRightPower = (yd - xd - rx) / denominator;
            double backRightPower = (yd + xd - rx) / denominator;

            leftFront.setPower(0.8 * frontLeftPower);
            leftBack.setPower(0.8 * backLeftPower);
            rightFront.setPower(0.8 * frontRightPower);
            rightBack.setPower(0.8 * backRightPower);

            if (gamepad2.y) {
                intake.setPower(0.7);
            }

            if(!gamepad2.x) {
                validResult = limelight.checkTag();

                if (validResult) {
                    limelight.turretAdjustment(turret);
                    distanceToTagCenter = limelight.getDistance();
                    xValue = limelight.getXValue();
                    validShooting = limelight.isValidShooting(xValue, distanceToTagCenter);
                    if (validShooting) {
                        shooter.setVelocity(distanceToTagCenter);
                    }
            } else {
                    if (gamepad2.a) {
                        shooter.setVelocity(-1200);
                    } else if (gamepad2.b) {
                        shooter.setVelocity(-1350);
                    }
                }


            }
        }




    }
}

