package com.example.meepmeeptesting1;

import com.acmerobotics.roadrunner.Pose2d;
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

        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(35, 60, Math.toRadians(270)))
                /* RED LEFT

                Start: -35, -60, 90
                .lineToY(-10)
                .turn(Math.toRadians(-90))
                .lineToX(-30)

                 */

                /* RED RIGHT
                Start: 20, -60, 90
                .lineToY(-53)
                .turn(Math.toRadians(-90))
                .lineToX(50)
                 */

                /* BLUE RIGHT
                Start: -16, 60, 270
                .lineToY(54)
                .turn(Math.toRadians(90))
                .lineToX(-50)

                 */
                        .lineToY(10)
                        .turn(Math.toRadians(-90))
                        .lineToX(30)



                .build());

        meepMeep.setBackground(MeepMeep.Background.FIELD_INTO_THE_DEEP_JUICE_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}