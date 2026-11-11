package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

public final class UnavailablePieceSensor implements PieceSensor {
    @Override public boolean piecePresent() { return false; }
    @Override public boolean available() { return false; }
}
