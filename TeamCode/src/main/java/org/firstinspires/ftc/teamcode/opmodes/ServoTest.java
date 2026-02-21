package org.firstinspires.ftc.teamcode.opmodes;



import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@Disabled
@TeleOp(name = "Servo Test",group = "A")
public class ServoTest extends LinearOpMode {

    public Servo servo1;
    public Servo servo2;
    public Servo servo3;

    @Override
    public void runOpMode() {
        servo1 = hardwareMap.get(Servo.class, "servo1");
        servo2 = hardwareMap.get(Servo.class, "servo2");
        servo3 = hardwareMap.get(Servo.class, "servo3");

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
                if (servo1.getPosition() == 0.4) {
                    servo1.setPosition(0);
                    servo2.setPosition(0);
                    servo3.setPosition(0);
                }
                else {
                    servo1.setPosition(0.4);
                    servo2.setPosition(0.4);
                    servo3.setPosition(0.4);
                }
            }
           else if (gamepad2.y) {
                if (servo1.getPosition() == 0.5) {
                    servo1.setPosition(0);
                    servo2.setPosition(0);
                    servo3.setPosition(0);
                }
                else {
                    servo1.setPosition(0.5);
                    servo2.setPosition(0.5);
                    servo3.setPosition(0.5);
                }
            }
            else if (gamepad2.b){
                if (servo1.getPosition() == 0.3) {
                    servo1.setPosition(0);
                    servo2.setPosition(0);
                    servo3.setPosition(0);
                }
                else {
                    servo1.setPosition(0.3);
                    servo2.setPosition(0.3);
                    servo3.setPosition(0.3);
                }
            }

            // Pace this loop so jaw action is reasonable speed.
            sleep(50);
        }
    }
}