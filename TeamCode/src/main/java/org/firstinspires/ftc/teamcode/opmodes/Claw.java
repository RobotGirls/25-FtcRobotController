package org.firstinspires.ftc.teamcode.opmodes;



import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class Claw {
    private CRServo claw;
    private CRServo claw2;

    public Claw(HardwareMap hardwareMap) {
        claw = hardwareMap.get(CRServo.class, "claw");
        claw2 = hardwareMap.get(CRServo.class, "claw2");
    }

    public class CloseClaw implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            claw.setPower(1);
            claw2.setPower(-1);
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            claw.setPower(0);
            claw2.setPower(0);
            return false;
        }
    }

    public Action closeClaw() {
        return new CloseClaw();
    }

    public class OpenClaw implements Action {
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            claw.setPower(-1);
            claw2.setPower(1);
            try {
                sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            claw.setPower(0);
            claw2.setPower(0);
            return false;
        }
    }

    public Action openClaw() {
        return new OpenClaw();
    }
}