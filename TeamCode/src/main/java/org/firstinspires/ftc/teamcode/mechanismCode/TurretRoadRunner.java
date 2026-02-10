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
    private ElapsedTime timer;
    private Telemetry telemetry1;
    private double turretPower = 0.5;  // FIXME figure out what value we want
    private double power = 0;
    private double error;
    private Limelight3ASensor myLimelight;
    private final int ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative = 0;
    private double integralSum = 0;
    private double Kp = 0.014; //Tx range is 0 to 26--> at max offset 26, when Kp is 0.02, speed is half power
    private double Ki = 0;
    private double Kd = 0;
    private final int TURRET_POSITION = 2000;


    public TurretRoadRunner(HardwareMap hardwareMap, Telemetry telemetry, Limelight3ASensor limelight) {
        turret = hardwareMap.get(DcMotor.class, "turret");
        timer = new ElapsedTime();
        telemetry1 = telemetry;
        myLimelight = limelight;
    }

    public class AimTurret implements Action {
        public boolean initialized = false;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                initialized = true;
                timer.reset();
            }
            double timerValue = timer.milliseconds();
            if (myLimelight.isLimeValid()) {
                error = myLimelight.getLimeTx();
                timer.reset();
                if (Math.abs(error) > ALIGN_THRESHOLD) {
                    error = -1 * myLimelight.getLimeTx();
                    derivative = (error - lastError) / timer.seconds();
                    integralSum = integralSum + (error * timer.seconds());
                    power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
                    turret.setPower(power);
                    lastError = error;
                    return true;
                } else {
                    turret.setPower(0);
                    return false;
                }
            } else {
                // if we don't see an apriltag
//                turret.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
//                //FIXME change encoder value to find the right one
//                if (turret.getCurrentPosition() >= -TURRET_POSITION &&
//                        turret.getCurrentPosition() <= TURRET_POSITION) {
//                    turret.setPower(turretPower);
//                    telemetry1.addData("Current Motor Position", turret.getCurrentPosition());
//                } else {
//                    turret.setPower(0);
//                    telemetry1.addData("Current Motor Position", "Too Far!");
                if (timerValue < 3000) {
                    return true;
                } else {
                    turret.setPower(0);
//                }  // turret posiion
                } // if limelight valid
                return true;
            } // run

        } // class AimTurret


    } // class
    public Action aimTurret() {
        return new TurretRoadRunner.AimTurret();
    }
}