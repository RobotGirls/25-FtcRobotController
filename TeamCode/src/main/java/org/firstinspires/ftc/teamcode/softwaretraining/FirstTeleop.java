package org.firstinspires.ftc.teamcode.softwaretraining;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Basic Mecanum TeleOp coda", group="software class")
public class FirstTeleop extends LinearOpMode {

    // Instantiate the hardware class
    RobotHardware robot = new RobotHardware();

    // Instantiate the Gamepad wrapper that handles its own
    // configuration menu
    GamepadHandler gamepadWrapper;

    @Override
    public void runOpMode() {
        // Initialize the hardware using the robot's hardwareMap built into the OpMode
        robot.init(hardwareMap);

        // Initialize our custom gamepad handler wrapper
        gamepadWrapper = new GamepadHandler(gamepad1);

        // Configuration Menu Loop (Runs while waiting for the match to start)
        // This allows you to view or change settings up until the match begins
        while (!isStarted() && !isStopRequested()) {
            gamepadWrapper.runConfigurationMenu(telemetry);
            telemetry.addLine("Press PLAY to lock settings and start TeleOp.");
            telemetry.update();
        }

        // --- MATCH STARTS HERE (Driver pressed PLAY) ---

        // Optional: Read the configured values if your TeleOp logic depends on them
        boolean isRedAlliance = gamepadWrapper.isRedAlliance();
        boolean isCloseSide   = gamepadWrapper.isCloseSide();

        // Run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            gamepadWrapper.update();

            // Run your drive control method
            handleMecanumDrive();

            // Telemetry data for active match monitoring
            telemetry.addData("Status", "Running");
            telemetry.addData("Selected Alliance", isRedAlliance ? "RED" : "BLUE");
            telemetry.addData("Selected Side", isCloseSide ? "CLOSE" : "FAR");
            telemetry.update();
        }  // end while
    } // end runOpMode

    /**
     * Isolated method specifically for drive commands
     */
    private void handleMecanumDrive() {
        double y  = gamepadWrapper.getDriveY();
        double x  = gamepadWrapper.getDriveX();
        double rx = gamepadWrapper.getTurnX();

        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1.0);

        double frontLeftPower  = (y + x + rx) / denominator;
        double backLeftPower   = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower  = (y + x - rx) / denominator;

        robot.setDrivePower(frontLeftPower, frontRightPower, backLeftPower, backRightPower);
    } // end handleMecanumDrive

} // end FirstTeleop class
