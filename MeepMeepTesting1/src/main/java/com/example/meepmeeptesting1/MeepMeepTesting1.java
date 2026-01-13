package com.example.meepmeeptesting1;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting1 {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();

        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .lineToY(-2)
                .turn(Math.toRadians(30))
                .splineToSplineHeading(new Pose2d(-10,-28, Math.toRadians(-90)),-50)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(-10,-50, Math.toRadians(-90)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(-16,-2, Math.toRadians(-130)),-55)
                .waitSeconds(0.1)
                .splineToSplineHeading(new Pose2d(20,-20,Math.toRadians(0)),-55)

                .build());

        meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_JUICE_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}