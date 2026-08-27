package org.firstinspires.ftc.teamcode.mechtesting;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import org.firstinspires.ftc.teamcode.mechtesting.RubberBandIntake;

@TeleOp(name = "ProtoTypeTesting")
public class PrototypeTeleop extends LinearOpMode {

    RubberBandIntake intake = new RubberBandIntake();
    RobotHardware hardware = new RobotHardware();

    float leftPower;
    float rightPower;

    @Override
    public void runOpMode() {

        intake.init(hardwareMap);
        hardware.hwInit(hardwareMap, telemetry);

        // Wait for the game to start (driver presses START)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            leftPower = -gamepad1.left_stick_y;
            rightPower = -gamepad1.right_stick_y;

            telemetry.addData("Left Power", leftPower);
            telemetry.addData("Right POwer", rightPower);


            hardware.leftSide.setPower(leftPower);
            hardware.rightSide.setPower(rightPower);

            telemetry.update();

            if (gamepad2.a) {
                intake.setPower(1);
            }
            else if (gamepad2.b) {
                intake.setPower(-1);
            } else {
                intake.setPower(0);
            }

        }
    }
}

