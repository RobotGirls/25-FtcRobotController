package org.firstinspires.ftc.teamcode.mechtesting;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class RobotHardware {

    HardwareMap hardwareMap;

    DcMotor leftSide;
    DcMotor rightSide;

    float leftPower;

    float rightPower;


    public void hwInit (HardwareMap hwMap, Telemetry telemetry) {

        hardwareMap = hwMap;

        leftSide = hardwareMap.get(DcMotor.class, "leftSide");
        rightSide = hardwareMap.get(DcMotor.class, "rightSide");

        leftSide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightSide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        leftSide.setDirection(DcMotorSimple.Direction.REVERSE);

        telemetry.addData(">", "Robot Ready.  Press START.");    //
        telemetry.update();

    }

}
