package org.firstinspires.ftc.teamcode.biobuzz2627.mechCode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class ShooterPurePursuit {
    public static final double SHOOT_VELOCITY = -1325;
    public static final double OUTTAKE_VELOCITY = 1340;

    private final DcMotorEx shooter;
    private final Telemetry telemetry;
    private final ElapsedTime timer = new ElapsedTime();

    public ShooterPurePursuit(HardwareMap hardwareMap, Telemetry telemetry) {
        shooter = hardwareMap.get(DcMotorEx.class, "shooter");
        shooter.setVelocityPIDFCoefficients(10, 3, 3, 2);
        shooter.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.telemetry = telemetry;
    }

    /** Starts the shooter directly; state-machine timing belongs to the autonomous OpMode. */
    public void startShooting() {
        shooter.setVelocity(SHOOT_VELOCITY);
    }

    public void startOuttake() {
        shooter.setVelocity(OUTTAKE_VELOCITY);
    }

    public void stop() {
        shooter.setVelocity(0);
    }

    public void addTelemetry() {
        telemetry.addData("Shooter target velocity", SHOOT_VELOCITY);
        telemetry.addData("Shooter velocity", shooter.getVelocity());
    }

    public class ShootArtifact implements Action {
        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                shooter.setVelocity(SHOOT_VELOCITY);
                initialized = true;
                timer.reset();
            }
            double timerValue = timer.milliseconds();
            telemetry.addData("Shooter target velocity", SHOOT_VELOCITY);
            telemetry.addData("Shooter velocity", shooter.getVelocity());
            telemetry.addData("Shooter Timer", timerValue);
            telemetry.update();
            if (timerValue < 4000) {
                return true;
            } else {
                shooter.setVelocity(0);
                return false;
            }
        }
    }

    public Action shootArtifact() {
        return new ShootArtifact();
    }
}
