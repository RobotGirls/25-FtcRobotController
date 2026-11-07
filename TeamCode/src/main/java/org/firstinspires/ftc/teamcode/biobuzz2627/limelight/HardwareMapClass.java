package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class HardwareMapClass {
    public DcMotor leftFront = null;
    public DcMotor rightFront = null;
    public DcMotor rightBack = null;
    public DcMotor leftBack = null;
    public DcMotor intake;
    public DcMotor turret;
    Telemetry telemetry1;

    public HardwareMapClass(HardwareMap hardwareMap, Telemetry telemetry) {
        telemetry1 = telemetry;

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

        intake = hardwareMap.get(DcMotor.class, "intake");
        turret = hardwareMap.get(DcMotor.class, "turret");
    }

    public void drive(Gamepad gamepad1) {
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
    }

    public void runIntake(double speed) {
        intake.setPower(speed);
    }


}
