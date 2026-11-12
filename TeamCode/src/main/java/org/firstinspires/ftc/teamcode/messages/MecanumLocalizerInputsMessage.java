package org.firstinspires.ftc.teamcode.messages;

import com.acmerobotics.roadrunner.ftc.PositionVelocityPair;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

public final class MecanumLocalizerInputsMessage {
    public long timestamp;
    public PositionVelocityPair frontLeft;
    public PositionVelocityPair backLeft;
    public PositionVelocityPair backRight;
    public PositionVelocityPair frontRight;
    public double yaw;
    public double pitch;
    public double roll;

    public MecanumLocalizerInputsMessage(PositionVelocityPair frontLeft, PositionVelocityPair backLeft, PositionVelocityPair backRight, PositionVelocityPair frontRight, YawPitchRollAngles angles) {
        this.timestamp = System.nanoTime();
        this.frontLeft = frontLeft;
        this.backLeft = backLeft;
        this.backRight = backRight;
        this.frontRight = frontRight;
        {
            this.yaw = angles.getYaw(AngleUnit.RADIANS);
            this.pitch = angles.getPitch(AngleUnit.RADIANS);
            this.roll = angles.getRoll(AngleUnit.RADIANS);
        }
    }
}
