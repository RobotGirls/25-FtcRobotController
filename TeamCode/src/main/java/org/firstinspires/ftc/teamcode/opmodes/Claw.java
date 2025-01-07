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

    private final double POWER_CLAW_OFF = 0.0;
    private final double POWER_CLAW_1_CLOSE = 1.0;
    private final double POWER_CLAW_2_CLOSE = -1.0;
    private final double POWER_CLAW_1_OPEN = -1.0;
    private final double POWER_CLAW_2_OPEN = 1.0;

    public Claw(HardwareMap hardwareMap) {
        claw = hardwareMap.get(CRServo.class, "claw");
        claw2 = hardwareMap.get(CRServo.class, "claw2");
    }

    public class CloseClaw implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            claw.setPower(POWER_CLAW_1_CLOSE);
            claw2.setPower(POWER_CLAW_2_CLOSE);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
            }
            claw.setPower(POWER_CLAW_OFF);
            claw2.setPower(POWER_CLAW_OFF);
            return false;
        }
    }

    public Action closeClaw() {
        return new CloseClaw();
    }

    public class OpenClaw implements Action {
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            claw.setPower(POWER_CLAW_1_OPEN);
            claw2.setPower(POWER_CLAW_2_OPEN);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
            }
            claw.setPower(POWER_CLAW_OFF);
            claw2.setPower(POWER_CLAW_OFF);
            return false;
        }
    }

    public Action openClaw() {
        return new OpenClaw();
    }

}
