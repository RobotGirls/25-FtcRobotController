package org.firstinspires.ftc.teamcode.softwaretraining;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class RobotHardware {
    // Declare drive motors
    public DcMotor frontLeft = null;
    public DcMotor frontRight = null;
    public DcMotor backLeft = null;
    public DcMotor backRight = null;

    // Local copy of hardware map
    private HardwareMap hwMap = null;


    /*--------------------------------------------------------------
     * Initialize standard hardware interfaces
     * @param ahwMap The Robot's Hardware Map
     */
    public void init(HardwareMap ahwMap) {
        hwMap = ahwMap;

        // Define and Initialize Motors using the exact names from the
        // Driver Station config
        frontLeft = hwMap.get(DcMotor.class, "frontLeft");
        frontRight = hwMap.get(DcMotor.class, "frontRight");
        backLeft = hwMap.get(DcMotor.class, "backLeft");
        backRight = hwMap.get(DcMotor.class, "backRight");

        // Set motor directions (Adjust based on your physical robot's gearing)
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        backRight.setDirection(DcMotor.Direction.REVERSE);

        setDrivePower(0, 0, 0, 0);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Set all motors to run without encoders for TeleOp
        //setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

    }

    /**
     * Convenience method to set all drive motor powers quickly
     */
    public void setDrivePower(double fl, double fr, double bl, double br) {
        frontLeft.setPower(fl);
        frontRight.setPower(fr);
        backLeft.setPower(bl);
        backRight.setPower(br);
    }
    public void Telemetry(Telemetry telemetry) {
        telemetry.addData("frontLeft", frontLeft);
        telemetry.addData("frontRight", frontRight);
        telemetry.addData("backLeft", backLeft);
        telemetry.addData("backRight", backRight);
        telemetry.update();
}
}
