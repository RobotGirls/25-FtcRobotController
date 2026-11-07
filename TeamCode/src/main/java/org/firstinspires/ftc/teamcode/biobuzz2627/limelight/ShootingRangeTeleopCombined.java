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
    boolean validShooting = false;
    double xValue;
    double distanceToTagCenter;

    @Override
    public void runOpMode() {

        HardwareMapClass robot = new HardwareMapClass(hardwareMap, telemetry);

        if (gamepad2.dpad_up) {
            isRed = false;
        } else if (gamepad2.dpad_down) {
            isRed = true;
        }

        LimelightSensor limelight = new LimelightSensor(hardwareMap, telemetry, isRed);
        shooter = new Shooter(hardwareMap, telemetry);

        telemetry.addData(">", "Robot Ready.  Press Play.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {

            robot.drive(gamepad1);

            if (!gamepad2.x) {
                validResult = limelight.checkTag();

                if (validResult) {
                    limelight.turretAdjustment(robot.turret);
                    distanceToTagCenter = limelight.getDistance();
                    xValue = limelight.getXValue();
                    validShooting = limelight.isValidShooting(xValue, distanceToTagCenter);
                    if (validShooting) {
                        shooter.setVelocity(-1200);
                    }
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

