package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@TeleOp(name = "Shooting Range Teleop", group = "A")
public class ShootingRangeTeleopCombined extends LinearOpMode {
    Shooter shooter;

    boolean isRed = false;

    boolean validResult = false;
    boolean inShootingRange = false;
    double xValue;
    double distanceToTagCenter;

    @Override
    public void runOpMode() {

        HardwareMapClass robot = new HardwareMapClass(hardwareMap, telemetry);

        if (gamepad2.dpad_up) {
            isRed = false; // blue
        } else if (gamepad2.dpad_down) {
            isRed = true; // red
        }

        LimelightSensor limelight = new LimelightSensor(hardwareMap, telemetry, isRed);
        shooter = new Shooter(hardwareMap, telemetry);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            robot.drive(gamepad1);

            if (!gamepad2.x) {
                limelight.turretAdjustment(robot.turret);
                inShootingRange = limelight.isShootingValid();
                if (inShootingRange) {
                    shooter.setVelocity(limelight.getDistanceToTag());
                }

            } else if (gamepad2.xWasPressed()) {
                    if (gamepad2.a) {
                        shooter.setVelocity(-1200);
                    } else if (gamepad2.b) {
                        shooter.setVelocity(-1350);
                    }

                    robot.turret.setPower(gamepad2.left_stick_x);
                }

            }
        }




    }

