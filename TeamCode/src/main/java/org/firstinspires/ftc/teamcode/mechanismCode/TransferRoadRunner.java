package org.firstinspires.ftc.teamcode.mechanismCode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import com.acmerobotics.roadrunner.Action;


public class TransferRoadRunner {

    private DcMotor transfer;
    private ElapsedTime timer1;
    private Telemetry telemetry1;

    public TransferRoadRunner(HardwareMap hardwareMap, Telemetry telemetry) {
        transfer = hardwareMap.get(DcMotor.class, "transfer");
        timer1 = new ElapsedTime();
        telemetry1 = telemetry;
        transfer.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public class IntakeArtifact implements Action {

        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                transfer.setPower(1);
                initialized = true;
                timer1.reset();
            }
            double timerValue = timer1.milliseconds();
            telemetry1.addData("Intake Timer",timerValue);
            telemetry1.update();
            if (timerValue < 6000) {
                return true;
            } else {
                transfer.setPower(0);
                return false;
            }
        }
    }
    public Action intakeArtifact() {
        return new TransferRoadRunner.IntakeArtifact();
    }

    public class OuttakeArtifact implements Action {
        private boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                transfer.setPower(-0.7);
                initialized = true;
                timer1.reset();
            }
            double timerValue = timer1.milliseconds();
            telemetry1.addData("Intake Timer",timerValue);
            telemetry1.update();
            if (timerValue < 5000) {
                return true;
            } else {
                transfer.setPower(0);
                return false;
            }
        }
    }

    public Action outtakeArtifact() {
        return new TransferRoadRunner.OuttakeArtifact();
    }
}