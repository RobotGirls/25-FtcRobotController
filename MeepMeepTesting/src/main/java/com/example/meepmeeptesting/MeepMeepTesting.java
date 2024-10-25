package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.Pose2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();

        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-38, -62, Math.toRadians(89)))

                // RED LEFT
<<<<<<< HEAD
                .lineToYSplineHeading(33, Math.toRadians(0))
=======

                .lineToY(-14)
                .turn(Math.toRadians(-90))
                .lineToX(-26)
                //.turn(Math.toRadians(90))

>>>>>>> d85fd02e1a0574561e7a4a3e219d32b932d98326

                /*
                // RED RIGHT
                .lineToX(30)
                .turn(Math.toRadians(90))
                .lineToY(30)
                .turn(Math.toRadians(90))
                 */

                .build());

        meepMeep.setBackground(MeepMeep.Background.FIELD_INTO_THE_DEEP_JUICE_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}