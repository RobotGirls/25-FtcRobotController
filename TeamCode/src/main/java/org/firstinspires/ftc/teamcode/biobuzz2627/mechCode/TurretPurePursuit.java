package org.firstinspires.ftc.teamcode.biobuzz2627.mechCode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Action;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;

public class TurretPurePursuit {
    DcMotor turret;
    private ElapsedTime timer;
    private Telemetry telemetry1;
    private double turretPower = 0.8;  // FIXME figure out what value we want
    private double power = 0;
    private LimelightPurePursuit myLimelight;
    private final double ALIGN_THRESHOLD = 3;
    private double lastError = 0;
    private double derivative = 0;
    private double integralSum = 0;
    private final double KP = 0.03; //Tx range is 0 to 26--> at max offset 26, when Kp is 0.02, speed is half power
    private final double KI = 0.03;
    private final double KD = 0;

    boolean aimingEnabled = true;


    public TurretPurePursuit(HardwareMap hardwareMap, Telemetry telemetry, LimelightPurePursuit limelight) {
        turret = hardwareMap.get(DcMotor.class, "turret");
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        timer = new ElapsedTime();
        telemetry1 = telemetry;
        myLimelight = limelight;
    }

    public class AimTurret implements Action {
        private boolean initialized = false;
        private double lastTime = 0;

        @Override
        public boolean run(@NonNull TelemetryPacket packet) {
            if (!initialized) {
                timer.reset();
                lastTime = timer.seconds();
                initialized = true;
            }

            if (!myLimelight.isLimeValid()) {
                turret.setPower(0);
                return true; // keep waiting
            }

            double error = -myLimelight.getLimeTx();
            double currentTime = timer.seconds();
            double dt = currentTime - lastTime;

            derivative = (error - lastError) / dt;
            integralSum += error * dt;
            double power = (KP * error) + (KI * integralSum) + (KD * derivative);
            turret.setPower(power);

            lastError = error;
            lastTime = currentTime;

            telemetry1.addData("Turret Error", error);
            telemetry1.addData("Turret Power", power);
            telemetry1.update();

            // stop when aligned
            if (Math.abs(error) < ALIGN_THRESHOLD) {
                turret.setPower(0);
                return false; // action complete
            }

            return true; // keep running
        }
    }

    public Action aimTurret() {
        return new TurretPurePursuit.AimTurret();
    }

    // FIXME: talk with mentors about continuous turret aiming in auto
    public Action aimTurretContinuous() {
        return packet -> {

            if (!aimingEnabled) {
                turret.setPower(0);
                return true;
            }

            if (!myLimelight.isLimeValid()) {
                turret.setPower(0);
                return true;
            }

            double error = -myLimelight.getLimeTx();
            double power = KP * error;

            turret.setPower(power);

            telemetry1.addData("LL valid", myLimelight.isLimeValid());
            telemetry1.addData("tx", myLimelight.getLimeTx());
            telemetry1.addData("turret power", power);
            telemetry1.update();

            return true; // never ends
        };
    }

}