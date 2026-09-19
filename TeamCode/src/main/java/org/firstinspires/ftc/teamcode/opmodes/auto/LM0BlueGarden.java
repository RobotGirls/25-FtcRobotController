package org.firstinspires.ftc.teamcode.opmodes.auto;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.MecanumDrive;

public class LM0BlueGarden {
    @Autonomous(name = "LM0 Blue Garden", group = "Blue Auto")
    public class LM0AutoBlue extends LinearOpMode {
        private boolean first = true;


        @Override
        public void runOpMode() throws InterruptedException {

            // telemetry.setAutoClear(false);
            // liftTimer.reset();
            // instantiating the robot at a specific pose
            Pose2d initialPose = new Pose2d(-70, 38, Math.toRadians(0));
            MecanumDrive drive = new MecanumDrive(hardwareMap, initialPose);
        }
    }
}
