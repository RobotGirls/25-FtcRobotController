package org.firstinspires.ftc.teamcode.opmodes;



import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;


@TeleOp(name = "DECODE TELEOP motor only",group = "A")
public class DECODEJustShooter extends LinearOpMode {

    public DcMotor shooter1;
    public DcMotor shooter2;

    public enum FlywheelState {
        ON,
        OFF
    }
    FlywheelState flywheelState = FlywheelState.OFF;

    public double shooterSpeed = 0.75;

    @Override
    public void runOpMode() {

        shooter1 = hardwareMap.get(DcMotor.class, "shooter1");
        shooter2 = hardwareMap.get(DcMotor.class,"shooter2");
        shooter1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
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

            if (gamepad2.x) {
                shooter1.setPower(shooterSpeed);
                shooter2.setPower(-1*shooterSpeed);
                flywheelState = FlywheelState.ON;
            }
           else if (gamepad2.b) {
                shooter1.setPower(-1*shooterSpeed);
                shooter2.setPower(shooterSpeed);
                flywheelState = FlywheelState.ON;
            }
            else {
                shooter1.setPower(0);
                shooter2.setPower(0);
            }

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }
}