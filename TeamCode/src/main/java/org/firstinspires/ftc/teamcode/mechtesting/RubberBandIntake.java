package org.firstinspires.ftc.teamcode.mechtesting;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class RubberBandIntake {
    DcMotor intake;

     public void init(HardwareMap hwMap) {

        intake = hwMap.get(DcMotor.class, "intakeMotor");

    }

    public void setPower(float intakePower) {
        intake.setPower(intakePower);
    }
}
