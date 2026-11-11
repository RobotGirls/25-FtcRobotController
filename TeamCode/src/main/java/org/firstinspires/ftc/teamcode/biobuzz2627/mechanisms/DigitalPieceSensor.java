package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

import com.qualcomm.robotcore.hardware.DigitalChannel;

public final class DigitalPieceSensor implements PieceSensor {
    private final DigitalChannel input;
    private final boolean activeLow;

    public DigitalPieceSensor(DigitalChannel input, boolean activeLow) {
        this.input = input;
        this.activeLow = activeLow;
        input.setMode(DigitalChannel.Mode.INPUT);
    }

    @Override
    public boolean piecePresent() {
        return activeLow ? !input.getState() : input.getState();
    }

    @Override
    public boolean available() {
        return true;
    }
}
