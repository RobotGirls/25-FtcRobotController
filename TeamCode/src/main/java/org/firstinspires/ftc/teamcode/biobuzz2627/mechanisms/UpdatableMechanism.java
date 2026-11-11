package org.firstinspires.ftc.teamcode.biobuzz2627.mechanisms;

public interface UpdatableMechanism {
    void update();
    void stop();
    boolean faulted();
    String status();
}
