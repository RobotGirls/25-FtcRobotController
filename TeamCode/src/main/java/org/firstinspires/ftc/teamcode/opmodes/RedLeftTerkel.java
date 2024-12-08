package org.firstinspires.ftc.teamcode.opmodes;


import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;


@Autonomous(name = "RedLeftTerkel")
public class RedLeftTerkel extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private FourWheelDirectDrivetrain drivetrain;

    private OneWheelDirectDrivetrain liftMotorDrivetrain;
    private DcMotor liftMotor;

    private OneWheelDirectDrivetrain liftPivotMotorDrivetrain;
    private DcMotor liftPivot;

    private Servo intake;


    DeadReckonPath driveToBasketPath;
    DeadReckonPath liftPivotUp;
    DeadReckonPath liftUp;
    DeadReckonPath parkPath;


    private Telemetry.Item whereAmI;

    /*
     * The default event handler for the robot.
     */

    @Override
    public void handleEvent(RobotEvent e)
    {
        /*
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }


    private void driveToPark(DeadReckonPath driveToParkPath)
    {
        whereAmI.setValue("in driveToPark");
        RobotLog.i("drives to First Ground Junction");

        this.addTask(new DeadReckonTask(this, driveToParkPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("finished parking");
                    liftPivot(liftPivotUp);

                }
            }
        });
    }

    private void liftPivot(DeadReckonPath driveToParkPath)
    {
        whereAmI.setValue("in driveToPark");
        RobotLog.i("drives to First Ground Junction");

        this.addTask(new DeadReckonTask(this, driveToParkPath, drivetrain){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("finished parking");

                }
            }
        });
    }




    public void delay(int seconds)
    {
        this.addTask(new SingleShotTimerTask(this, 1000*seconds) {
            @Override
            public void handleEvent (RobotEvent e){
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                switch(event.kind) {
                    case EXPIRED:
                        break;
                }
            }
        });
    }



    public void initPaths()
    {

        driveToBasketPath = new DeadReckonPath();
        liftPivotUp = new DeadReckonPath();
        liftUp = new DeadReckonPath();
        parkPath = new DeadReckonPath();

        driveToBasketPath.stop();
        liftPivotUp.stop();
        liftUp.stop();
        parkPath.stop();

        driveToBasketPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,2, 0.7);
        driveToBasketPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,2, 0.7);

        liftPivotUp.addSegment(DeadReckonPath.SegmentType.STRAIGHT,2, 0.3);

        liftUp.addSegment(DeadReckonPath.SegmentType.STRAIGHT,5, 0.7);

        //liftToSmallJunctionPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, 0.5);



    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "leftFront");
        frontRight = hardwareMap.get(DcMotor.class, "rightFront");
        backLeft = hardwareMap.get(DcMotor.class, "leftBack");
        backRight = hardwareMap.get(DcMotor.class, "rightBack");
        liftMotor = hardwareMap.get(DcMotor.class, "liftMotor");
        liftPivot = hardwareMap.get(DcMotor.class, "liftPivot");

        intake = hardwareMap.servo.get("intake");

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        liftPivot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        liftPivot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        liftMotorDrivetrain = new OneWheelDirectDrivetrain(liftMotor);
        liftMotorDrivetrain.resetEncoders();
        liftMotorDrivetrain.encodersOn();

        liftPivotMotorDrivetrain = new OneWheelDirectDrivetrain(liftPivot);
        liftPivotMotorDrivetrain.resetEncoders();
        liftPivotMotorDrivetrain.encodersOn();


        whereAmI = telemetry.addData("location in code", "init");
        initPaths();
    }

    @Override
    public void start()
    {
        driveToPark(driveToBasketPath);
        whereAmI.setValue("in Start");
    }
}