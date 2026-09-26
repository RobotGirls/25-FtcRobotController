package org.firstinspires.ftc.teamcode.opmodes.auto.mechCode;



import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Intake {
    private DcMotor intake;

    public Intake(HardwareMap hardwareMap) {
        intake = hardwareMap.get(DcMotor.class, "intake");

    }

    public class CloseClaw implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            intake.setPower(1);

            try {
                sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            intake.setPower(0);
            return false;
        }
    }

    public Action closeClaw() {
        return new CloseClaw();
    }

    public class OpenClaw implements Action {
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            intake.setPower(-1);

            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            intake.setPower(0);
            return false;
        }
    }

    public Action openClaw() {
        return new OpenClaw();
    }
}