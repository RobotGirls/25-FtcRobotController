package org.firstinspires.ftc.teamcode.messages;

public final class MecanumCommandMessage {
    public long timestamp;
    public double voltage;
    public double frontLeftPower;
    public double backLeftPower;
    public double backRightPower;
    public double frontRightPower;

    public MecanumCommandMessage(double voltage, double frontLeftPower, double backLeftPower, double backRightPower, double frontRightPower) {
        this.timestamp = System.nanoTime();
        this.voltage = voltage;
        this.frontLeftPower = frontLeftPower;
        this.backLeftPower = backLeftPower;
        this.backRightPower = backRightPower;
        this.frontRightPower = frontRightPower;
    }
}
