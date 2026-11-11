package org.firstinspires.ftc.teamcode.biobuzz2627.robot;

import com.acmerobotics.roadrunner.Pose2d;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.teamcode.MecanumDrive;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.DigitalPieceSensor;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.IntakeStateMachine;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.PieceSensor;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.ShooterStateMachine;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.ShotCoordinator;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.TransferStateMachine;
import org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms.UnavailablePieceSensor;
import org.firstinspires.ftc.teamcode.biobuzz2627.strategy.AutoStrategy;
import org.firstinspires.ftc.teamcode.biobuzz2627.vision.LimelightPoseCorrector;
import org.firstinspires.ftc.teamcode.biobuzz2627.vision.LimelightService;
import org.firstinspires.ftc.teamcode.biobuzz2627.vision.LimelightTargeting;

/** Owns current-season hardware and provides one deterministic subsystem update order. */
public final class BioBuzzRobot {
    public final MecanumDrive drive;
    public final LimelightService limelight;
    public final LimelightTargeting targeting;
    public final LimelightPoseCorrector poseCorrector = new LimelightPoseCorrector();
    public final ShooterStateMachine shooter;
    public final IntakeStateMachine intake;
    public final TransferStateMachine transfer;
    public final ShotCoordinator shot;
    public final RobotHealth health;

    public BioBuzzRobot(HardwareMap hardwareMap, Pose2d initialPose, AutoStrategy strategy) {
        drive = new MecanumDrive(
                hardwareMap, initialPose, AutomationConfig.mecanumLocalizerMode());
        limelight = new LimelightService(hardwareMap.get(Limelight3A.class, "limelight"));
        targeting = new LimelightTargeting(strategy.alliance, strategy.shootingSide);

        PieceSensor intakeSensor = sensor(hardwareMap, AutomationConfig.INTAKE_SENSOR_NAME);
        PieceSensor transferSensor = sensor(hardwareMap, AutomationConfig.TRANSFER_SENSOR_NAME);
        shooter = new ShooterStateMachine(hardwareMap.get(
                DcMotorEx.class, AutomationConfig.SHOOTER_NAME));
        intake = new IntakeStateMachine(hardwareMap.get(
                DcMotorEx.class, AutomationConfig.INTAKE_NAME), intakeSensor);
        transfer = new TransferStateMachine(hardwareMap.get(
                DcMotorEx.class, AutomationConfig.TRANSFER_NAME), transferSensor);
        shot = new ShotCoordinator(shooter, transfer, targeting);
        health = new RobotHealth(intakeSensor, transferSensor);
    }

    public void start() { limelight.start(); }

    /** Call once per loop. Navigation separately owns drive pose updates while following. */
    public void updateVisionAndMechanisms() {
        limelight.update(Math.toDegrees(drive.localizer.getPose().heading.toDouble()));
        targeting.update(limelight.latest());
        intake.update();
        shot.update();
    }

    /** Returns true when a bounded Limelight correction was applied. */
    public boolean updatePoseFromLimelight() {
        return poseCorrector.update(drive, limelight.latest(), targeting);
    }

    public void stopAll() {
        intake.stop();
        shot.stop();
        limelight.stop();
    }

    public boolean mechanismFaulted() {
        return intake.faulted() || shot.faulted();
    }

    public String mechanismStatus() {
        return "intake=" + intake.status() + ", shot=" + shot.status();
    }

    private static PieceSensor sensor(HardwareMap hardwareMap, String name) {
        if (!AutomationConfig.PIECE_SENSORS_VERIFIED) return new UnavailablePieceSensor();
        DigitalChannel channel = hardwareMap.tryGet(DigitalChannel.class, name);
        return channel == null ? new UnavailablePieceSensor()
                : new DigitalPieceSensor(channel, AutomationConfig.PIECE_SENSOR_ACTIVE_LOW);
    }
}
