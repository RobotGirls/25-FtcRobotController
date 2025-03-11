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
            wrist.setPosition(0.5);
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

    public class WristIn implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.2);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristIn() {
        return new WristIn();
    }

    public class WristIntake implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.728);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristIntake() {
        return new WristIntake();
    }

    public class WristScore implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.95);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristScore() {
        return new WristScore();
    }

    public class WristSpecimen implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.89);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristSpecimen() {
        return new WristSpecimen();
    }

    public class WristOutOfTheWay implements Action {

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            wrist.setPosition(0.68);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
    }

    public Action wristOutOfTheWay() {
        return new WristOutOfTheWay();
    }
}
