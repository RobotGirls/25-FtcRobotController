//package org.firstinspires.ftc.teamcode.mechanismCode;
//
//import androidx.annotation.NonNull;
//
//import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
//import com.acmerobotics.roadrunner.Action;
//import com.qualcomm.hardware.limelightvision.LLResult;
//import com.qualcomm.hardware.limelightvision.Limelight3A;
//import com.qualcomm.robotcore.hardware.DcMotor;
//import com.qualcomm.robotcore.hardware.HardwareMap;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.robotcore.external.Telemetry;
//import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
//
//
//public class TurretRoadRunner {
//
//    private DcMotor transfer;
//    private ElapsedTime timer1;
//    private Telemetry telemetry1;
//    private DcMotor turret;
//    private Limelight3A limelight;
//    private double error;
//
//    private double lastError = 0;
//    private double derivative = 0;
//    private double integralSum = 0;
//
//    private double Kp = 0.0165; // Tx range is 0 to 26 --> at max offset 26, when Kp is 0.02, speed is half power
//    private double Ki = 0;
//    private double Kd = 0;
//    double power = 0; // initial turret power
//    final int ALIGN_THRESHOLD = 3;
//
//
//    public TurretRoadRunner(HardwareMap hardwareMap, Telemetry telemetry) {
//        transfer = hardwareMap.get(DcMotor.class, "transfer");
//        limelight = hardwareMap.get(Limelight3A.class, "limelight");
//        // actionBuilder builds from the drive steps passed to it
//         turret = hardwareMap.get(DcMotor.class, "turret");
//        timer1 = new ElapsedTime();
//        telemetry1 = telemetry;
//        telemetry1.setMsTransmissionInterval(11);
//
//        telemetry.setMsTransmissionInterval(11);
//
//        limelight.pipelineSwitch(0);
//
//        /*
//         * Starts polling for data.  If you neglect to call start(), getLatestResult() will return null.
//         */
//        limelight.start();
//
//    }
//
//    public class IntakeArtifact implements Action {
//
//        private boolean initialized = false;
//
//        @Override
//        public boolean run(@NonNull TelemetryPacket packet) {
//            if (!initialized) {
//
//
///*
//                telemetry.addData("tx", result1.getTx());
//                telemetry.addData("txnc", result1.getTxNC());
//                telemetry.addData("ty", result1.getTy());
//                telemetry.addData("tync", result1.getTyNC());
//
// */
//                while (Math.abs(error) > ALIGN_THRESHOLD) {
//                        error = result1.getTx();
//                        ElapsedTime timer = new ElapsedTime();
//                        if (Math.abs(error) > ALIGN_THRESHOLD) {
//                            error = -1 * result1.getTx();
//                            derivative = (error - lastError) / timer.seconds();
//                            integralSum = integralSum + (error * timer.seconds());
//                            power = (Kp * error) + (Ki * integralSum) + (Kd * derivative);
//                            turret.setPower(power);
//                            lastError = error;
//                        } else {
//                            turret.setPower(0);  // aligned
//                        }
//
//                    }
//                }
//    }
//    public Action intakeArtifact() {
//        return new TurretRoadRunner.IntakeArtifact();
//    }
//
//
//}