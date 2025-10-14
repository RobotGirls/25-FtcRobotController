//package org.firstinspires.ftc.teamcode.opmodes;
//
//import com.acmerobotics.dashboard.config.Config;
//import com.qualcomm.hardware.dfrobot.HuskyLens;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.CRServo;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.robotcore.internal.system.Deadline;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//
//@TeleOp(name = "Teleop LM0")
//public class ITDTeleopLM2 extends LinearOpMode {
//
//    /* Declare OpMode members. */
//    public DcMotor  leftFront   = null;
//    public DcMotor  rightFront  = null;
//    public DcMotor  rightBack  = null;
//    public DcMotor  leftBack  = null;
//
//    public DcMotor shooter;
//    public CRServo claw;
//    public DcMotor liftPivot;
//    public CRServo claw2;
//
//    private HuskyLens huskyLens;
//    private final int READ_PERIOD = 1;
//
//    public static final int CENTER = 160;
//    private static final int ALIGN_THRESHOLD = 10;
//
//    public static double Kp = 0.002;
//    public static double Ki = 0.0005;
//    public static double Kd = 0.0;
//    double integralSum;
//    double lastError;
//
//
//    @Override
//    public void runOpMode() {
//
//        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
//
//        // Define and Initialize Motors
//        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
//        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
//        rightBack = hardwareMap.get(DcMotor.class, "backRight");
//        leftBack = hardwareMap.get(DcMotor.class, "backLeft");
//
//        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
//        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
//
//        //RNRRMecanumDrive drive = new RNRRMecanumDrive(hardwareMap, new Pose2d(0, 0, 0));
//        //drive.setPoseEstimate(startPose);
//
//        shooter = hardwareMap.get(DcMotor.class, "shooter");
//
//
//        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//        liftPivot.setTargetPosition(0);
//        liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//        liftPivot.setPower(0.5);
//
//
//        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
//        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
//        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips
//
//        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
//
//
//        rateLimit.expire();
//
//        /*
//         * Basic check to see if the device is alive and communicating.  This is not
//         * technically necessary here as the HuskyLens class does this in its
//         * doInitialization() method which is called when the device is pulled out of
//         * the hardware map.  However, sometimes it's unclear why a device reports as
//         * failing on initialization.  In the case of this device, it's because the
//         * call to knock() failed.
//         */
//        if (!huskyLens.knock()) {
//            telemetry.addData(">>", "Problem communicating with " + huskyLens.getDeviceName());
//        } else {
//            telemetry.addData(">>", "Press start to continue");
//        }
//
//        /*
//         * The device uses the concept of an algorithm to determine what types of
//         * objects it will look for and/or what mode it is in.  The algorithm may be
//         * selected using the scroll wheel on the device, or via software as shown in
//         * the call to selectAlgorithm().
//         *
//         * The SDK itself does not assume that the user wants a particular algorithm on
//         * startup, and hence does not set an algorithm.
//         *
//         * Users, should, in general, explicitly choose the algorithm they want to use
//         * within the OpMode by calling selectAlgorithm() and passing it one of the values
//         * found in the enumeration HuskyLens.Algorithm.
//         *
//         * Other algorithm choices for FTC might be: OBJECT_RECOGNITION, COLOR_RECOGNITION or OBJECT_CLASSIFICATION.
//         */
//        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
//
//
//
//        // Send telemetry message to signify robot waiting;
//        telemetry.addData(">", "Robot Ready.  Press START.");    //
//        telemetry.update();
//
//        // Wait for the game to start (driver presses START)
//        waitForStart();
//
//        // run until the end of the match (driver presses STOP)
//        while (opModeIsActive()) {
//            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
//            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
//            double rx = gamepad1.right_stick_x;
//
//            // Denominator is the largest motor power (absolute value) or 1
//            // This ensures all the powers maintain the same ratio,
//            // but only if at least one is out of the range [-1, 1]
//            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
//            double frontLeftPower = (y + x + rx) / denominator;
//            double backLeftPower = (y - x + rx) / denominator;
//            double frontRightPower = (y - x - rx) / denominator;
//            double backRightPower = (y + x - rx) / denominator;
//
//            leftFront.setPower(frontLeftPower);
//            leftBack.setPower(backLeftPower);
//            rightFront.setPower(frontRightPower);
//            rightBack.setPower(backRightPower);
//
//
//
//            if (gamepad2.a) {
//                shooter.setPower(0.75);
//            }
//            else if (gamepad2.b) {
//                shooter.setPower(0.9);
//            }
//            else if (gamepad2.x) {
//                shooter.setPower(1);
//            }
//
//            // Pace this loop so jaw action is reasonable speed.
//            sleep(50);
//        }
//    }
//}
//
///*
//Copyright (c) 2023 FIRST
//
//All rights reserved.
//
//Redistribution and use in source and binary forms, with or without modification,
//are permitted (subject to the limitations in the disclaimer below) provided that
//the following conditions are met:
//
//Redistributions of source code must retain the above copyright notice, this list
//of conditions and the following disclaimer.
//
//Redistributions in binary form must reproduce the above copyright notice, this
//list of conditions and the following disclaimer in the documentation and/or
//other materials provided with the distribution.
//
//Neither the name of FIRST nor the names of its contributors may be used to
//endorse or promote products derived from this software without specific prior
//written permission.
//
//NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
//LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
//THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
//FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
//DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
//CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
//TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
//THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//*/
//package org.firstinspires.ftc.teamcode.opmodes;
//
//
//import com.acmerobotics.dashboard.config.Config;
//import com.qualcomm.hardware.dfrobot.HuskyLens;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.DcMotorSimple;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.robotcore.internal.system.Deadline;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///*
// * This OpMode illustrates how to use the DFRobot HuskyLens.
// *
// * The HuskyLens is a Vision Sensor with a built-in object detection model.  It can
// * detect a number of predefined objects and AprilTags in the 36h11 family, can
// * recognize colors, and can be trained to detect custom objects. See this website for
// * documentation: https://wiki.dfrobot.com/HUSKYLENS_V1.0_SKU_SEN0305_SEN0336
// *
// * For detailed instructions on how a HuskyLens is used in FTC, please see this tutorial:
// * https://ftc-docs.firstinspires.org/en/latest/devices/huskylens/huskylens.html
// *
// * This sample illustrates how to detect AprilTags, but can be used to detect other types
// * of objects by changing the algorithm. It assumes that the HuskyLens is configured with
// * a name of "huskylens".
// *
// * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
// * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
// */
//@Config
//@TeleOp(name = "HuskyLens for aiming", group = "Sensor")
//public class HuskyLensForRobotAiming extends LinearOpMode {
//
//    private final int READ_PERIOD = 1;
//
//    private HuskyLens huskyLens;
//
//    public DcMotor  leftFront   = null;
//    public DcMotor  rightFront  = null;
//    public DcMotor  rightBack  = null;
//    public DcMotor  leftBack  = null;
//
//    public static final int CENTER = 160;
//    private static final int ALIGN_THRESHOLD = 10;
//
//    public static double Kp = 0.002;
//    public static double Ki = 0.0005;
//    public static double Kd = 0.0;
//    double integralSum;
//    double lastError;
//    ElapsedTime timer = new ElapsedTime();
//
//    @Override
//    public void runOpMode()
//    {
//        huskyLens = hardwareMap.get(HuskyLens.class, "huskylens");
//        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
//        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
//        rightBack = hardwareMap.get(DcMotor.class, "backRight");
//        leftBack = hardwareMap.get(DcMotor.class, "backLeft");
//
//        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        leftFront.setDirection(DcMotorSimple.Direction.REVERSE);
//        leftBack.setDirection(DcMotorSimple.Direction.REVERSE);
//
//
//
//        /*
//         * This sample rate limits the reads solely to allow a user time to observe
//         * what is happening on the Driver Station telemetry.  Typical applications
//         * would not likely rate limit.
//         */
//        Deadline rateLimit = new Deadline(READ_PERIOD, TimeUnit.SECONDS);
//
//        /*
//         * Immediately expire so that the first time through we'll do the read.
//         */
//        rateLimit.expire();
//
//        /*
//         * Basic check to see if the device is alive and communicating.  This is not
//         * technically necessary here as the HuskyLens class does this in its
//         * doInitialization() method which is called when the device is pulled out of
//         * the hardware map.  However, sometimes it's unclear why a device reports as
//         * failing on initialization.  In the case of this device, it's because the
//         * call to knock() failed.
//         */
//        if (!huskyLens.knock()) {
//            telemetry.addData(">>", "Problem communicating with " + huskyLens.getDeviceName());
//        } else {
//            telemetry.addData(">>", "Press start to continue");
//        }
//
//        /*
//         * The device uses the concept of an algorithm to determine what types of
//         * objects it will look for and/or what mode it is in.  The algorithm may be
//         * selected using the scroll wheel on the device, or via software as shown in
//         * the call to selectAlgorithm().
//         *
//         * The SDK itself does not assume that the user wants a particular algorithm on
//         * startup, and hence does not set an algorithm.
//         *
//         * Users, should, in general, explicitly choose the algorithm they want to use
//         * within the OpMode by calling selectAlgorithm() and passing it one of the values
//         * found in the enumeration HuskyLens.Algorithm.
//         *
//         * Other algorithm choices for FTC might be: OBJECT_RECOGNITION, COLOR_RECOGNITION or OBJECT_CLASSIFICATION.
//         */
//        huskyLens.selectAlgorithm(HuskyLens.Algorithm.TAG_RECOGNITION);
//
//        telemetry.update();
//        waitForStart();
//
//        /*
//         * Looking for AprilTags per the call to selectAlgorithm() above.  A handy grid
//         * for testing may be found at https://wiki.dfrobot.com/HUSKYLENS_V1.0_SKU_SEN0305_SEN0336#target_20.
//         *
//         * Note again that the device only recognizes the 36h11 family of tags out of the box.
//         */
//        while(opModeIsActive()) {
//            if (!rateLimit.hasExpired()) {
//                continue;
//            }
//            rateLimit.reset();
//
//            /*
//             * All algorithms, except for LINE_TRACKING, return a list of Blocks where a
//             * Block represents the outline of a recognized object along with its ID number.
//             * ID numbers allow you to identify what the device saw.  See the HuskyLens documentation
//             * referenced in the header comment above for more information on IDs and how to
//             * assign them to objects.
//             *
//             * Returns an empty array if no objects are seen.
//             */
//
//
//            List<HuskyLens.Block> blocks = Arrays.asList(huskyLens.blocks(1));
//            if (!blocks.isEmpty()) {
//                HuskyLens.Block block = blocks.get(0);
//
//                int x = block.x;
//                int offset = x - CENTER;
//
//                if (Math.abs(offset) > ALIGN_THRESHOLD) {
//                    double derivative = (offset - lastError) / timer.seconds();
//                    integralSum = integralSum + (offset * timer.seconds());
//                    double power = (Kp * offset) + (Ki * integralSum) + (Kd * derivative);
////                            double power = 0.0009 * offset;
////                            power = Math.max(-0.3, Math.min(0.3, power));
//                    lastError = offset;
//                    leftFront.setPower(-power);
//                    leftBack.setPower(-power);
//                    rightFront.setPower(power);
//                    rightBack.setPower(power);
//                    timer.reset();
//
//                } else {
//                    leftFront.setPower(0); // aligned
//                    rightBack.setPower(0);
//                    leftBack.setPower(0);
//                    rightFront.setPower(0);
//                }
//
//                telemetry.addData("Tag X", x);
//                telemetry.addData("Offset", offset);
//                telemetry.addData("Block", block.toString());
//            } else {
//                // turret.setPower(0);  // no tag seen
//                telemetry.addLine("No tag detected");
//            }
//            telemetry.update();
//
//        }
//        telemetry.update();
//    }
//}
