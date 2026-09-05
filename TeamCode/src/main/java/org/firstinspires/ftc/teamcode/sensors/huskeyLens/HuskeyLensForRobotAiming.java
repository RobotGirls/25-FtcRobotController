
package org.firstinspires.ftc.teamcode.sensors.huskeyLens;


import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;


import org.firstinspires.ftc.robotcore.internal.system.Deadline;

import java.util.Arrays;
import java.util.List;

/*
 * This OpMode illustrates how to use the DFRobot HuskyLens.
 *
 * The HuskyLens is a Vision Sensor with a built-in object detection model.  It can
 * detect a number of predefined objects and AprilTags in the 36h11 family, can
 * recognize colors, and can be trained to detect custom objects. See this website for
 * documentation: https://wiki.dfrobot.com/HUSKYLENS_V1.0_SKU_SEN0305_SEN0336
 *
 * For detailed instructions on how a HuskyLens is used in FTC, please see this tutorial:
 * https://ftc-docs.firstinspires.org/en/latest/devices/huskylens/huskylens.html
 *
 * This sample illustrates how to detect AprilTags, but can be used to detect other types
 * of objects by changing the algorithm. It assumes that the HuskyLens is configured with
 * a name of "huskylens".
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */
@Config
@TeleOp(name = "HuskyLens for aiming", group = "Sensor")
public class HuskeyLensForRobotAiming extends LinearOpMode {

    private HuskyLens huskyLens;

    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public static final int CENTER = 160;
    private static final int ALIGN_THRESHOLD = 10;

    public static double Kp = 0.002;
    public static double Ki = 0.0005;
    public static double Kd = 0.0;
    double integralSum;
    double lastError;
    ElapsedTime timer = new ElapsedTime();
   HuskeyLensSensor huskeyLensSensor = new HuskeyLensSensor();

    @Override
    public void runOpMode()
    {
        huskeyLensSensor.instanciateMotorsAndSensor(hardwareMap, telemetry, huskeyLensSensor);
        Deadline rateLimit = huskeyLensSensor.tagReadTime(huskeyLensSensor);



        telemetry.update();
        waitForStart();

        /*
         * Looking for AprilTags per the call to selectAlgorithm() above.  A handy grid
         * for testing may be found at https://wiki.dfrobot.com/HUSKYLENS_V1.0_SKU_SEN0305_SEN0336#target_20.
         *
         * Note again that the device only recognizes the 36h11 family of tags out of the box.
         */
        while(opModeIsActive()) {
            if (!rateLimit.hasExpired()) {
                continue;
            }
            rateLimit.reset();

            /*
             * All algorithms, except for LINE_TRACKING, return a list of Blocks where a
             * Block represents the outline of a recognized object along with its ID number.
             * ID numbers allow you to identify what the device saw.  See the HuskyLens documentation
             * referenced in the header comment above for more information on IDs and how to
             * assign them to objects.
             *
             * Returns an empty array if no objects are seen.
             */


            List<HuskyLens.Block> blocks = Arrays.asList(huskyLens.blocks(1));
            if (!blocks.isEmpty()) {
                double offset = huskeyLensSensor.findCurrBlockAndOffset(blocks);

                if (Math.abs(offset) > ALIGN_THRESHOLD) {
                    double power = huskeyLensSensor.pidForPower(offset, lastError, timer, integralSum, Kp, Ki, Kd);
//                            double power = 0.0009 * offset;
//                            power = Math.max(-0.3, Math.min(0.3, power));
                    lastError = offset;
                    leftFront.setPower(-power);
                    leftBack.setPower(-power);
                    rightFront.setPower(power);
                    rightBack.setPower(power);
                    timer.reset();

                } else {
                    leftFront.setPower(0); // aligned
                    rightBack.setPower(0);
                    leftBack.setPower(0);
                    rightFront.setPower(0);
                }

                telemetry.addData("Tag X", huskeyLensSensor.findXValue(blocks));
                telemetry.addData("Offset", offset);
                telemetry.addData("Block", huskeyLensSensor.detectCurrBlock(blocks).toString());
            } else {
                // turret.setPower(0);  // no tag seen
                telemetry.addLine("No tag detected");
            }
            telemetry.update();

        }
        telemetry.update();
    }
}