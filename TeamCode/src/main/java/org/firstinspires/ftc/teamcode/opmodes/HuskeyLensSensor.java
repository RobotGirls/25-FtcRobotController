package org.firstinspires.ftc.teamcode.opmodes;

import com.qualcomm.hardware.dfrobot.HuskyLens;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.Arrays;
import java.util.List;

public class HuskeyLensSensor {
    public final int READ_PERIOD = 1;
    public HuskyLens huskyLens;
    public static final int CENTER = 160;
    public static final int ALIGN_THRESHOLD = 10;


    public double findCurrBlockAndOffset(List<HuskyLens.Block> blocks) {
        HuskyLens.Block block = detectCurrBlock(blocks);
        int x = findXValue(blocks);
        double calculateOffset = x - CENTER;
        return calculateOffset;
    }

    public int findXValue(List<HuskyLens.Block> blocks) {
        HuskyLens.Block block = detectCurrBlock(blocks);
        int x = block.x;
        return x;
    }

    public HuskyLens.Block detectCurrBlock(List<HuskyLens.Block> blocks) {
        HuskyLens.Block block = blocks.get(0);
        return  block;
    }

}
