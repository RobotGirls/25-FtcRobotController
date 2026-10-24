package org.firstinspires.ftc.teamcode.biobuzz2627.limelight;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class Shooter {
    private DcMotorEx shooter;

    Telemetry telemetry1;

    // Constructor
    public Shooter(HardwareMap hardwareMap, Telemetry telemetry) {
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        // shooterHood = hardwareMap.get(Servo.class, "shooterHood");

        // Adjust the loop values later
        shooter.setVelocityPIDFCoefficients(10,3,3,2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        telemetry1 = telemetry;

    }

    public void setVelocity(double tagDistance) {
        if (tagDistance <= -1.5) {
            shooter.setVelocity(-1200);
        } else if (tagDistance <= 1.5) {
            shooter.setVelocity(-1350);
        }
    }

}
