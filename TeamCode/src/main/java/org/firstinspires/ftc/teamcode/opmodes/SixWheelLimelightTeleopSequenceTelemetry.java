package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
@Disabled
@TeleOp(name = "ILT TELEOP SEQUENCE TELEMETRY")

public class SixWheelLimelightTeleopSequenceTelemetry extends LinearOpMode {


    public final double TURRET_OFFSET = 0; // FIXME figure out how many degrees to the side the turret will be aiming relative to front of robot

    @Override
    public void runOpMode() throws InterruptedException {

        ElapsedTime sequenceTimer = new ElapsedTime();

        telemetry.setMsTransmissionInterval(11);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();


        telemetry.setMsTransmissionInterval(11);

        double power = 0; // initial turret power

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {

            if (gamepad2.x) {
                sequenceTimer.reset();
                sequenceTimer.startTime();
            }

            if (sequenceTimer.milliseconds() < 5000) {
                telemetry.addData("Sequence Status:", "Shooter speeds up");
                telemetry.addData("Timer Status:", sequenceTimer.milliseconds());

            } else if (sequenceTimer.milliseconds() < 8000) {
                telemetry.addData("Sequence Status:", "transfer outtakes :D");
                telemetry.addData("Timer Status:", sequenceTimer.milliseconds());

            } else if (sequenceTimer.milliseconds() < 10000) {
                telemetry.addData("Sequence Status:", "Run intake + transfer, shoot ball");
                telemetry.addData("Timer Status:", sequenceTimer.milliseconds());

            }

            telemetry.update();

        }
    }

}