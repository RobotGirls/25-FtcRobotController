package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HuskeyLensSensor {
    public final int READ_PERIOD = 1;
    public HuskyLens huskyLens;
    public static final int CENTER = 160;
    public static final int ALIGN_THRESHOLD = 10;
    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public void instanciateMotorsAndSensor(HardwareMap hardwareMap, Telemetry telemetry, HuskeyLensSensor huskeyLensSensor) {
        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);


        /*
         * Basic check to see if the device is alive and communicating.  This is not
         * technically necessary here as the HuskyLens class does this in its
         * doInitialization() method which is called when the device is pulled out of
         * the hardware map.  However, sometimes it's unclear why a device reports as
         * failing on initialization.  In the case of this device, it's because the
         * call to knock() failed.
         */
        if (!huskyLens.knock()) {
            telemetry.addData(">>", "Problem communicating with " + huskyLens.getDeviceName());
        } else {
            telemetry.addData(">>", "Press start to continue");
        }

        Deadline rateLimit = tagReadTime(huskeyLensSensor);

        /*
         * The device uses the concept of an algorithm to determine what types of
         * objects it will look for and/or what mode it is in.  The algorithm may be
         * selected using the scroll wheel on the device, or via software as shown in
         * the call to selectAlgorithm().
         *
         * The SDK itself does not assume that the user wants a particular algorithm on
         * startup, and hence does not set an algorithm.
         *
         * Users, should, in general, explicitly choose the algorithm they want to use
         * within the OpMode by calling selectAlgorithm() and passing it one of the values
         * found in the enumeration HuskyLens.Algorithm.
         *
         * Other algorithm choices for FTC might be: OBJECT_RECOGNITION, COLOR_RECOGNITION or OBJECT_CLASSIFICATION.
         */
        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);


    }

    public Deadline tagReadTime(HuskeyLensSensor huskeyLensSensor) {
        /*
         * This sample rate limits the reads solely to allow a user time to observe
         * what is happening on the Driver Station telemetry.  Typical applications
         * would not likely rate limit.
         */
        Deadline rateLimit = new Deadline(huskeyLensSensor.READ_PERIOD, TimeUnit.SECONDS);

        /*
         * Immediately expire so that the first time through we'll do the read.
         */
        rateLimit.expire();

        return rateLimit;

    }

    public double findCurrBlockAndOffset(List<HuskyLens.Block> blocks) {
        HuskyLens.Block block = detectCurrBlock(blocks);
        int x = findXValue(blocks);
        return x - CENTER;
    }

    public int findXValue(List<HuskyLens.Block> blocks) {
        HuskyLens.Block block = detectCurrBlock(blocks);
        return block.x;
    }

    public HuskyLens.Block detectCurrBlock(List<HuskyLens.Block> blocks) {
        return blocks.get(0);
    }

    public double pidForPower(double offset, double lastError, ElapsedTime timer, double integralSum, double Kp, double Ki, double Kd) {
        double derivitive = (offset - lastError) / timer.seconds();
        integralSum = integralSum + (offset * timer.seconds());
        return (Kp * offset) + (Ki * integralSum) + (Kd * derivitive);
    }

}
