package org.firstinspires.ftc.teamcode.mechanismCode;


import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretRoadRunner {
    DcMotor turret;
    private ElapsedTime timer1;
    private Telemetry telemetry1;
    private double turretPower = 0;
    private double error;
    private Limelight3ASensor myLimelight;


    public TurretRoadRunner(HardwareMap hardwareMap, Telemetry telemetry, Limelight3ASensor limelight) {
        turret = hardwareMap.get(DcMotor.class, "turret");
        timer1 = new ElapsedTime();
        telemetry1 = telemetry;
        myLimelight = limelight;
    }

    public class AimTurret implements Action {
        public boolean initialized = false;
        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            error =


        }

    }
}