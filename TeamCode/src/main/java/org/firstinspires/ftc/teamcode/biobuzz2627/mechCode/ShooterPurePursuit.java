package org.firstinspires.ftc.teamcode.biobuzz2627.mechCode;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterPurePursuit {
    public static final double SHOOT_VELOCITY = -1325;
    public static final double OUTTAKE_VELOCITY = 1340;
    public static final double HOOD_POSITION = 0.25;

    private final DcMotorEx shooter;
    private final Servo shooterHood;
    private final Telemetry telemetry;

    public ShooterPurePursuit(HardwareMap hardwareMap, Telemetry telemetry) {
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        shooterHood = hardwareMap.get(Servo.class, "shooterHood");
        shooter.setVelocityPIDFCoefficients(10, 3, 3, 2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.telemetry = telemetry;
    }

    /** Starts the shooter directly; state-machine timing belongs to the autonomous OpMode. */
    public void startShooting() {
        shooterHood.setPosition(HOOD_POSITION);
        shooter.setVelocity(SHOOT_VELOCITY);
    }

    public void startOuttake() {
        shooterHood.setPosition(HOOD_POSITION);
        shooter.setVelocity(OUTTAKE_VELOCITY);
    }

    public void stop() {
        shooter.setVelocity(0);
    }

    public void addTelemetry() {
        telemetry.addData("Shooter target velocity", SHOOT_VELOCITY);
        telemetry.addData("Shooter velocity", shooter.getVelocity());
    }
}
