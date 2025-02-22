package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

@Disabled
@TeleOp(name = "Teleop ILT TWO MOTORS")
public class ITDTeleopTWOMOTORS extends LinearOpMode {

    /* Declare OpMode members. */
    public DcMotor  leftFront   = null;
    public DcMotor  rightFront  = null;
    public DcMotor  rightBack  = null;
    public DcMotor  leftBack  = null;

    public DcMotor lift;
    public CRServo claw;
    public DcMotor liftPivot;
    public DcMotor liftPivot2;
    public CRServo claw2;

    public enum LiftState {
        LIFT_START,
        LIFT_EXTEND_BASKET,
        LIFT_RESET_ENCODER,
        LIFT_RUN_WITH_ENCODER,
        LIFT_CHECK
    }
    public enum LiftPivotState {
        PIVOT_CHECK,
        PIVOT_START,
        PIVOT_INTO_SUBMERSIBLE,
        PIVOT_SUBMERSIBLE_OVERIRDE_RUN_ENCODER,
        PIVOT_RESET_ENCODER
    }
    public enum ClawState {
        CLAW_CHECK,
        CLAW_INTAKE,
        CLAW_OUTTAKE,
        CLAW_OUTTAKE_SLOW,
        CLAW_ZERO_POWER
    }


    LiftState liftState = LiftState.LIFT_RESET_ENCODER;
    LiftPivotState liftPivotState = LiftPivotState.PIVOT_RESET_ENCODER;
    ClawState clawState = ClawState.CLAW_CHECK;


    @Override
    public void runOpMode() {

        // Define and Initialize Motors
        leftFront = hardwareMap.get(DcMotor.class, "frontLeft");
        rightFront = hardwareMap.get(DcMotor.class, "frontRight");
        rightBack = hardwareMap.get(DcMotor.class, "backRight");
        leftBack = hardwareMap.get(DcMotor.class, "backLeft");

        rightFront.setDirection(DcMotorSimple.Direction.REVERSE);
        rightBack.setDirection(DcMotorSimple.Direction.REVERSE);

        lift = hardwareMap.get(DcMotor.class, "lift");
        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");
        liftPivot2 = hardwareMap.get(DcMotor.class, "liftPivot2");
        claw = hardwareMap.get(CRServo.class, "claw");
        claw2 = hardwareMap.get(CRServo.class, "claw2");

        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        liftPivot2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        lift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels.  Gear Reduction or 90 Deg drives may require direction flips

        // Send telemetry message to signify robot waiting;
        telemetry.addData(">", "Robot Ready.  Press START.");    //
        telemetry.update();

        // Wait for the game to start (driver presses START)
        waitForStart();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            double y = -gamepad1.left_stick_y; // Remember, Y stick value is reversed
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;

            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio,
            // but only if at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator;
            double backLeftPower = (y - x + rx) / denominator;
            double frontRightPower = (y - x - rx) / denominator;
            double backRightPower = (y + x - rx) / denominator;

            leftFront.setPower(0.8*frontLeftPower);
            leftBack.setPower(0.8*backLeftPower);
            rightFront.setPower(0.8*frontRightPower);
            rightBack.setPower(0.8*backRightPower);
            liftPivot.setPower(0.7*gamepad2.right_stick_y);
            liftPivot2.setPower(-0.7*gamepad2.right_stick_y);

            telemetry.addData("Lift encoder ticks: ", lift.getCurrentPosition());
            telemetry.update();

//         //   old claw code
//            if (gamepad2.a) {
//                claw.setPower(1);
//                claw2.setPower(-1);
//            }
//            else if (gamepad2.x) {
//                claw.setPower(-1);
//                claw2.setPower(1);
//            }
//            // outtake slowly to slowly let out a specimen so the hook is exposed
//            else if (gamepad2.b) {
//                claw.setPower(-0.35);
//                claw2.setPower(0.35);
//            }
//            else {
//                claw.setPower(0);
//                claw2.setPower(0);
//            }
//

            // welcome to the wonderful world of switch-cases/state machines! Allow us to begin...
            switch (liftState) {
                case LIFT_CHECK:
                    if (gamepad2.right_stick_button) {
                        liftState = LiftState.LIFT_RESET_ENCODER;
                    } else if (gamepad2.dpad_up) {
                        liftState = LiftState.LIFT_EXTEND_BASKET;
                    } else if (gamepad2.dpad_down) {
                        liftState = LiftState.LIFT_RUN_WITH_ENCODER;
                    } else if (gamepad2.left_stick_y != 0) {
                        liftState = LiftState.LIFT_START;
                    }
                    break;

                case LIFT_START:
                    if (gamepad2.left_stick_y != 0 ) {
                        lift.setPower(-gamepad2.left_stick_y);
                    } else {
                        liftState = LiftState.LIFT_CHECK;
                    }
                    break;
            case LIFT_EXTEND_BASKET:
                if (gamepad2.dpad_down || gamepad2.left_stick_x > 0 || gamepad2.left_stick_y > 0 || gamepad2.right_stick_x > 0 || gamepad2.right_stick_y != 0) {
                    liftState = LiftState.LIFT_RUN_WITH_ENCODER;
                }
                else if (gamepad2.dpad_up ) {
                    liftPivot.setPower(1);
                    liftPivot2.setPower(1);
                    lift.setTargetPosition(1710);
                    lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                    liftState = LiftState.LIFT_CHECK;
                } else {
                    liftState = LiftState.LIFT_CHECK;
                }
                break;
                case LIFT_RUN_WITH_ENCODER:
                            lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            liftPivot2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            liftState = LiftState.LIFT_CHECK;
                        break;

                        case LIFT_RESET_ENCODER:
                            lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            liftState = LiftState.LIFT_CHECK;
                                break;

                                default:
                                    // should never be reached, as liftState should never be null
                                    liftState = LiftState.LIFT_CHECK;
                            }
                        }

        switch (liftPivotState) {
            case PIVOT_CHECK:
                if (gamepad2.dpad_left && liftPivot.getCurrentPosition() < 50 && lift.getCurrentPosition() < 30) {
                    liftPivotState = LiftPivotState.PIVOT_INTO_SUBMERSIBLE;
                }  else if (gamepad2.right_stick_y != 0) {
                    liftPivotState = LiftPivotState.PIVOT_START;
                } else if (gamepad2.right_stick_button) {
                    liftPivotState = LiftPivotState.PIVOT_RESET_ENCODER;
                } else if (gamepad2.dpad_down) {
                    liftPivotState = LiftPivotState.PIVOT_SUBMERSIBLE_OVERIRDE_RUN_ENCODER;
                }
                break;

            case PIVOT_START:
                if (gamepad2.right_stick_y != 0 ) {
                    liftPivot.setPower(gamepad2.right_stick_y);
                    liftPivot2.setPower(gamepad2.right_stick_y);
                } else {
                    liftPivotState = LiftPivotState.PIVOT_CHECK;
                }
                break;
            case PIVOT_INTO_SUBMERSIBLE:
                 if (gamepad2.dpad_down) {
                liftPivotState = LiftPivotState.PIVOT_SUBMERSIBLE_OVERIRDE_RUN_ENCODER;
            } else if (gamepad2.left_stick_x > 0 || gamepad2.left_stick_y > 0 || gamepad2.right_stick_x > 0 || gamepad2.right_stick_y > 0) {
                     liftPivotState = LiftPivotState.PIVOT_SUBMERSIBLE_OVERIRDE_RUN_ENCODER;
                 } else if (gamepad2.dpad_up) {
                         liftPivot.setDirection(DcMotorSimple.Direction.REVERSE);
                         liftPivot2.setDirection(DcMotorSimple.Direction.REVERSE);
                         liftPivot.setPower(-0.9);
                         liftPivot2.setPower(-0.9);
                         liftPivot.setTargetPosition(150);
                         liftPivot2.setTargetPosition(150);
                         liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                         liftPivot2.setMode(DcMotor.RunMode.RUN_TO_POSITION);
                        liftPivotState = LiftPivotState.PIVOT_CHECK;
                 } else {
                         liftPivotState = LiftPivotState.PIVOT_CHECK;
                     }

                break;
            case PIVOT_SUBMERSIBLE_OVERIRDE_RUN_ENCODER:
                    lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    liftPivot2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    liftPivotState = LiftPivotState.PIVOT_CHECK;
                    break;

            case PIVOT_RESET_ENCODER:
                lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
               liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
               liftPivot2.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

                liftPivotState = LiftPivotState.PIVOT_CHECK;
                break;

            default:
                // should never be reached, as liftPivotState should never be null
                liftPivotState = LiftPivotState.PIVOT_START;
        }


        // small optimization, instead of repeating ourselves in each
        // lift state case besides LIFT_START for the cancel action,
        // it's just handled here
//        if (gamepad2.y && liftState != LiftState.LIFT_START) {
//            liftState = LiftState.LIFT_START;
//        }

        // mecanum drive code goes here
        // But since none of the stuff in the switch case stops
        // the robot, this will always run!
        // updateDrive(gamepad1, gamepad2);

        switch (clawState) {
            case CLAW_CHECK:
                if (gamepad2.a) {
                    clawState = ClawState.CLAW_INTAKE;
                } else if (gamepad2.x) {
                    clawState = ClawState.CLAW_OUTTAKE;
                } else if (gamepad2.b) {
                    clawState = ClawState.CLAW_OUTTAKE_SLOW;
                } else {
                    clawState = ClawState.CLAW_ZERO_POWER;
                }
                break;
            case CLAW_INTAKE:
                if (gamepad2.a) {
                    claw.setPower(1);
                    claw2.setPower(-1);
                } else {
                    clawState = ClawState.CLAW_CHECK;
                }
                break;
            case CLAW_OUTTAKE:
                if (gamepad2.x) {
                    claw.setPower(-1);
                    claw2.setPower(1);
                } else {
                    clawState = ClawState.CLAW_CHECK;
                }
                break;
            case CLAW_OUTTAKE_SLOW:
                if (gamepad2.b) {
                    claw.setPower(-0.35);
                    claw2.setPower(0.35);
                } else {
                    clawState = ClawState.CLAW_CHECK;
                }
                break;

            case CLAW_ZERO_POWER:
                claw.setPower(0);
                claw2.setPower(0);
                clawState = ClawState.CLAW_CHECK;
                break;

            default:
                // should never be reached, as liftState should never be null
                liftPivotState = LiftPivotState.PIVOT_START;
        }


//            // old code - dispersed into the state machine, kept here just in case
            // at the beginning of teleop, reset encoders to 0 (lift and liftpivot have to be all the way down)

//            if (gamepad2.right_stick_button) {
//                lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//                liftPivot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
//            }
//            // lift up to high basket
//            if (gamepad2.dpad_up) {
//                liftPivot.setPower(1);
//                liftPivot.setTargetPosition(1710);
//                liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
//            }
            //
//            else if (gamepad2.dpad_down) {
//                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//                liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            }

//            // alternative to dpad down: if there is joystick input, override the run to position mode
//            if (gamepad2.left_stick_x > 0 || gamepad2.left_stick_y > 0 || gamepad2.right_stick_x > 0 || gamepad2.right_stick_y > 0) {
//                lift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//                liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
//            }

            // if the lift and lift pivot are all the way in and we want to go out into the submersible to get a sample, lift the pivot slightly so the intake doesn't get stuck and then extend
            if (gamepad2.dpad_left && liftPivot.getCurrentPosition() < 50 && lift.getCurrentPosition() < 30) {
                liftPivot.setDirection(DcMotorSimple.Direction.REVERSE);
                liftPivot.setPower(-0.9);
                liftPivot.setTargetPosition(150);
                liftPivot.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            }



            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }
