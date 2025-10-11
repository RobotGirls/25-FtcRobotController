package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name="Pinpoint reset")
public class PinpointReset extends LinearOpMode {

    public GoBildaPinpointDriver driver;

    public PinpointReset(GoBildaPinpointDriver driver) {
        this.driver = driver;
    }

    @Override
    public void runOpMode() {
        driver = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        waitForStart();


        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {
            driver.resetPosAndIMU();
        }
    }
}
