package org.firstinspires.ftc.teamcode.mechanismCode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import com.acmerobotics.roadrunner.Action;


public class IntakeRoadRunner {

        private DcMotor intake;
        private ElapsedTime timer1;
        private Telemetry telemetry1;

        public IntakeRoadRunner(HardwareMap hardwareMap, Telemetry telemetry) {
            intake = hardwareMap.get(DcMotor.class, "intake");
            timer1 = new ElapsedTime();
            telemetry1 = telemetry;

        }

        public class IntakeArtifact implements Action {

            private boolean initialized = false;

            @Override
            public boolean run(@NonNull TelemetryPacket packet) {
                if (!initialized) {
                    intake.setPower(1);
                    initialized = true;
                    timer1.reset();
                }
                double timerValue = timer1.milliseconds();
                telemetry1.addData("Intake Timer",timerValue);
                telemetry1.update();
                if (timerValue < 5000) {
                    return true;
                } else {
                    intake.setPower(0);
                    return false;
                }
            }
        }
        public Action intakeArtifact() {
            return new IntakeRoadRunner.IntakeArtifact();
        }

    public class OuttakeArtifact implements Action {
        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                intake.setPower(-0.7);
                initialized = true;
                timer1.reset();
            }
            double timerValue = timer1.milliseconds();
            telemetry1.addData("Intake Timer",timerValue);
            telemetry1.update();
            if (timerValue < 5000) {
                return true;
            } else {
                intake.setPower(0);
                return false;
            }
        }
    }

    public Action outtakeArtifact() {
        return new IntakeRoadRunner.OuttakeArtifact();
    }
}