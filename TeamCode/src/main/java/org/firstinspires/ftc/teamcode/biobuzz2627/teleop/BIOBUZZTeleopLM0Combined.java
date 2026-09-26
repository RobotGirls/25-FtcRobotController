package org.firstinspires.ftc.teamcode.biobuzz2627.teleop;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Basic Mecanum TeleOp coda", group="software class")
public class BIOBUZZTeleopLM0Combined extends LinearOpMode {
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
                // CRITICAL: Always update the gamepad state tracking at the top of the loop
                // If you do not update the gamepad state at the very top of the loop, your
                // rising-edge (when button is first pressed down, off->on) button detection (isAPressed(),
                // isBPressed(), etc.) will completely break, causing buttons to either lock
                // up, glitch, or register multiple times. How the Rising-Edge Logic Works
                // To detect a single discrete click, the code must compare the gamepad's
                // current state to its previous state from the last loop cycle.
                // - A button press is only considered a new "click" if:
                //   - The button is physically down now (gamepad.a == true)
                //   - AND the button was up in the last frame (prevA == false)
                //
                // Frame 1: Button UP   -> current = false, prev = false -> Click? NO
                // Frame 2: Button DOWN -> current = true,  prev = false -> Click? YES!
                // Frame 3: Button HELD -> current = true,  prev = true  -> Click? NO
                // 2. The Consequence of Misplacement
                // Your TeleOp loop runs incredibly fast—hundreds of times per second.
                // If you call driverGamepad.update() at the bottom of the loop or
                // forget it, the sequence of execution breaks down:
                //   Scenario A: Forgetting it entirely. If update() is never called, prevA
                // stays false forever. If a driver holds down the "A" button for
                // just 200ms, the robot loops about 50 times. Because prevA
                // is always false, the robot thinks the driver clicked the button 50
                // separate times. In a menu, this causes options to cycle uncontrollably.
                //   Scenario B: Calling it out of order
                // To detect a click, the robot must compare a "Past" and "Present" signal of
                // gamepad1.a. If you read the fresh data from gamepad1.a first, you
                // accidentally overwrite your history, making both variables identical and
                // leaving the robot unable to tell that a button click even happened.
                // By updating at the very top of the loop, you ensure that:
                // - The old "current" state safely becomes the prev state.
                // - The fresh, real-time hardware data from the internal FTC gamepad1 becomes the new "current" state.
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

    } // end of opmode class
