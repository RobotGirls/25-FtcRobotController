package org.firstinspires.ftc.teamcode.opmodes;



import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Wrist {
    private Servo wrist;

    public Wrist(HardwareMap hardwareMap) {
        wrist = hardwareMap.servo.get("wrist");
    }

    public class WristUp implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.6);
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristUp() {
        return new WristUp();
    }
}
