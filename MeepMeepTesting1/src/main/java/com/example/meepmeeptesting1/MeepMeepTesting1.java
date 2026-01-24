package com.example.meepmeeptesting1;

import com.acmerobotics.roadrunner.Action;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.TrajectoryActionBuilder;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting1 {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();
        RoadRunnerBotEntity myBot1 = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .setColorScheme(new ColorSchemeBlueDark())
                .build();

        myBot.runAction(myBot1.getDrive().actionBuilder(new Pose2d(-52, 46, Math.toRadians(130)))
                .setReversed(true)
                .splineTo(new Vector2d(-8,8),Math.toRadians(-45))
                 .turn(Math.toRadians(25))
                .splineTo(new Vector2d(-12,52),Math.toRadians(90))
                .setReversed(true)
                .splineTo(new Vector2d(-8,8),Math.toRadians(-45))
                .turn(Math.toRadians(90))
                .lineToX(2)
                .build());

        myBot1.runAction(myBot.getDrive().actionBuilder(new Pose2d(-52, -46, Math.toRadians(-130)))
                .setReversed(true)
                .splineTo(new Vector2d(-8,-8),Math.toRadians(45))
                .turn(Math.toRadians(25))
                .splineTo(new Vector2d(-12,-52),Math.toRadians(-90))
                .setReversed(true)
                .splineTo(new Vector2d(-8,-8),Math.toRadians(45))
                .turn(Math.toRadians(90))
                .lineToX(2)
                .build());

        meepMeep.setBackground(MeepMeep.Background.FIELD_DECODE_JUICE_DARK)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .addEntity(myBot1)
                .start();
    }
}