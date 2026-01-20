package org.firstinspires.ftc.teamcode.mechanismCode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import com.acmerobotics.roadrunner.Action;


public class ShooterRoadRunner {

    private DcMotorEx shooter;

    private Servo shooterHood;
    private ElapsedTime timer1;
    private Telemetry telemetry1;
    public double shooterSpeed;



    public ShooterRoadRunner(HardwareMap hardwareMap, Telemetry telemetry) {
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");

        shooter.setVelocityPIDFCoefficients(10,3,3,2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        timer1 = new ElapsedTime();
        telemetry1 = telemetry;

    }

    public class ShootArtifact implements Action {

        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                shooter.setVelocity(-1340);

                initialized = true;
                timer1.reset();
            }
            double timerValue = timer1.milliseconds();
            telemetry1.addData("timer",timerValue);
            telemetry1.update();
            if (timerValue < 6500) {
                return true;
            } else {
                shooter.setVelocity(0);

                return false;
            }
        }
    }
    public Action shootArtifact() {
        return new ShooterRoadRunner.ShootArtifact();
    }

    public class ArtifactOut implements Action {
        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                shooter.setVelocity(1340);
                initialized = true;
                timer1.reset();
            }
            double timerValue = timer1.milliseconds();
            telemetry1.addData("Intake Timer",timerValue);
            telemetry1.update();
            if (timerValue < 5000) {
                return true;
            } else {
                shooter.setPower(0);
                return false;
            }
        }
    }

    public Action artifactOut() {
        return new ShooterRoadRunner.ArtifactOut();
    }
}