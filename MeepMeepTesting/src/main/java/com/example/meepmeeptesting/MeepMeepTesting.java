package com.example.meepmeeptesting;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.roadrunner.DefaultBotBuilder;
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class MeepMeepTesting {
    public static void main(String[] args) {
        MeepMeep meepMeep = new MeepMeep(800);


        RoadRunnerBotEntity myBot = new DefaultBotBuilder(meepMeep)
                // Set bot constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(60, 60, Math.toRadians(180), Math.toRadians(180), 15)
                .build();

        myBot.runAction(myBot.getDrive().actionBuilder(new Pose2d(-70, 38, Math.toRadians(0)))
                .strafeToLinearHeading(new Vector2d(-50, 15), 0)
                .waitSeconds(1)
                .setTangent(Math.toRadians(90))
                .splineToLinearHeading(new Pose2d(new Vector2d(30, 60), 0), Math.toRadians(0))

//
                .build());

        // Load the custom BIOBUZZ field image
        BufferedImage biobuzzField = null;
        try {
            // Replace with the actual path to where you saved the image
            biobuzzField = ImageIO.read(new File("MeepMeepTesting/src/main/java/resources/biobuzz.png"));
        } catch (IOException e) {
            System.out.println("Could not load BIOBUZZ field image!");
            e.printStackTrace();
        }

        // Apply the custom image background
        if (biobuzzField != null) {
            meepMeep.setBackground(biobuzzField);
        } else {
            // Fallback to a built-in background if the image fails to load
            meepMeep.setBackground(MeepMeep.Background.GRID_GRAY);
        }

        meepMeep.setDarkMode(true)
                .setBackgroundAlpha(0.95f)
                .addEntity(myBot)
                .start();
    }
}
