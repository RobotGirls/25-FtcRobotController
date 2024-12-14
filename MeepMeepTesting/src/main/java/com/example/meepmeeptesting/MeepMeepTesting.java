package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeBlueDark;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);

        // Declare our first bot
        RoadRunnerBotEntity myFirstBot = new DefaultBotBuilder(meepMeep)
                // We set this bot to be blue
                .setColorScheme(new ColorSchemeBlueDark())
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();

        myFirstBot.runAction(myFirstBot.getDrive().actionBuilder(new Pose2d(8, -60, Math.toRadians(90)))
                .lineToY(-33.5)
              .turn(Math.toRadians(-90))
                .lineToX(34)
                .turn(Math.toRadians(-90))
                .strafeTo(new Vector2d(34,-4))
                .strafeTo(new Vector2d(48,-4))
                .strafeTo(new Vector2d(48,-58))
                .strafeTo(new Vector2d(48,-4))
                .strafeTo(new Vector2d(56,-4))
                .strafeTo(new Vector2d(56,-58))
                .strafeTo(new Vector2d(56,-4))
                .strafeTo(new Vector2d(65,-4))
                .strafeTo(new Vector2d(65,-58))






//     // red right speciman and push sample
//                .lineToY(-33.5)
//              .turn(Math.toRadians(-90))
//                .lineToX(34)
//                .turn(Math.toRadians(-90))
//                .strafeTo(new Vector2d(34,-4))
//                .strafeTo(new Vector2d(48,-4))
//                .strafeTo(new Vector2d(48,-58))


//                .turn(Math.toRadians(90))
//                .lineToY(0)
//                .turn(Math.toRadians(90))
                .build());

        // Declare out second bot
        RoadRunnerBotEntity mySecondBot = new DefaultBotBuilder(meepMeep)
                // We set this bot to be red
                .setColorScheme(new ColorSchemeRedDark())
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();


        mySecondBot.runAction(mySecondBot.getDrive().actionBuilder(new Pose2d(-38, -62, Math.toRadians(89)))
                // red left park
                .lineToY(-10)
                .turn(Math.toRadians(-90))
                .lineToX(-26)

//                // red left basket + samples (push)
//                .lineToY(-52)
//                .turn(Math.toRadians(90))
//                .lineToX(-52)
//                .turn(Math.toRadians(45))
//                .turn(Math.toRadians(45))
//      //          .splineTo(new Vector2d(-35,-52),180) too wavy
//                .strafeTo(new Vector2d(-35,-52))
//                .strafeTo(new Vector2d(-35,-10))
//                .strafeTo(new Vector2d(-46,-10))
//                .strafeTo(new Vector2d(-46,-60))
//                .strafeTo(new Vector2d(-46,-10))
//                .turn(Math.toRadians(90))
//                .lineToX(-26)

                .build());


        meepMeep.setBackground(MeepMeep.Background.FIELD_INTO_THE_DEEP_JUICE_LIGHT)
                .setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                // Add both of our declared bot entities
                .addEntity(myFirstBot)
                .addEntity(mySecondBot)
                .start();
    }
}