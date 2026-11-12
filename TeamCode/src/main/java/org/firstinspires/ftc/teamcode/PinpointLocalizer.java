package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.acmerobotics.roadrunner.Vector2d;
import com.acmerobotics.roadrunner.ftc.PinpointView;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;

import java.util.Objects;

@Config
public final class PinpointLocalizer implements Localizer {
    public static class Params {
        /** Sideways offset of the forward/parallel pod from the tracking point. */
        public double parallelPodYOffsetInches = 0.0;
        /** Forward offset of the sideways/perpendicular pod from the tracking point. */
        public double perpendicularPodXOffsetInches = 0.0;
        /** Flip if pushing the robot forward makes its reported X position decrease. */
        public boolean reverseParallelEncoder = false;
        /** Flip if pushing the robot left makes its reported Y position decrease. */
        public boolean reversePerpendicularEncoder = false;
    }

    public static Params PARAMS = new Params();

    public final GoBildaPinpointDriver driver;
    public final GoBildaPinpointDriver.EncoderDirection initialParDirection, initialPerpDirection;

    private Pose2d txWorldPinpoint;
    private Pose2d txPinpointRobot = new Pose2d(0, 0, 0);

    public PinpointLocalizer(HardwareMap hardwareMap, Pose2d initialPose) {
        // TODO: make sure your config has a Pinpoint device with this name
        //   see https://ftc-docs.firstinspires.org/en/latest/hardware_and_software_configuration/configuring/index.html
        driver = hardwareMap.get(GoBildaPinpointDriver.class, "pinpoint");

        driver.setEncoderResolution(
                GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD);
        driver.setOffsets(PARAMS.parallelPodYOffsetInches,
                PARAMS.perpendicularPodXOffsetInches, DistanceUnit.INCH);

        initialParDirection = PARAMS.reverseParallelEncoder
                ? GoBildaPinpointDriver.EncoderDirection.REVERSED
                : GoBildaPinpointDriver.EncoderDirection.FORWARD;
        initialPerpDirection = PARAMS.reversePerpendicularEncoder
                ? GoBildaPinpointDriver.EncoderDirection.REVERSED
                : GoBildaPinpointDriver.EncoderDirection.FORWARD;

        driver.setEncoderDirections(initialParDirection, initialPerpDirection);

        driver.resetPosAndIMU();

        txWorldPinpoint = initialPose;
    }

    public boolean isReady() {
        return driver.getDeviceStatus() == GoBildaPinpointDriver.DeviceStatus.READY;
    }

    public String getStatus() {
        return String.valueOf(driver.getDeviceStatus());
    }

    @Override
    public void setPose(Pose2d pose) {
        txWorldPinpoint = pose.times(txPinpointRobot.inverse());
    }

    @Override
    public Pose2d getPose() {
        return txWorldPinpoint.times(txPinpointRobot);
    }

    @Override
    public PoseVelocity2d update() {
        driver.update();
        if (Objects.requireNonNull(driver.getDeviceStatus()) == GoBildaPinpointDriver.DeviceStatus.READY) {
            txPinpointRobot = new Pose2d(driver.getPosX(DistanceUnit.INCH), driver.getPosY(DistanceUnit.INCH), driver.getHeading(UnnormalizedAngleUnit.RADIANS));
            Vector2d worldVelocity = new Vector2d(driver.getVelX(DistanceUnit.INCH), driver.getVelY(DistanceUnit.INCH));
            Vector2d robotVelocity = Rotation2d.fromDouble(-txPinpointRobot.heading.log()).times(worldVelocity);

            return new PoseVelocity2d(robotVelocity, driver.getHeadingVelocity(UnnormalizedAngleUnit.RADIANS));
        }
        return new PoseVelocity2d(new Vector2d(0, 0), 0);
    }

    /** Road Runner tuning view backed by this Pinpoint, including its internal heading sensor. */
    public PinpointView asRoadRunnerView() {
        return new PinpointView() {
            private GoBildaPinpointDriver.EncoderDirection parDirection = initialParDirection;
            private GoBildaPinpointDriver.EncoderDirection perpDirection = initialPerpDirection;

            @Override
            public void update() {
                driver.update();
            }

            @Override
            public int getParEncoderPosition() {
                return driver.getEncoderX();
            }

            @Override
            public int getPerpEncoderPosition() {
                return driver.getEncoderY();
            }

            @Override
            public float getHeadingVelocity() {
                return (float) driver.getHeadingVelocity(UnnormalizedAngleUnit.DEGREES);
            }

            @Override
            public void setParDirection(@NonNull DcMotorSimple.Direction direction) {
                parDirection = direction == DcMotorSimple.Direction.FORWARD
                        ? GoBildaPinpointDriver.EncoderDirection.FORWARD
                        : GoBildaPinpointDriver.EncoderDirection.REVERSED;
                driver.setEncoderDirections(parDirection, perpDirection);
            }

            @Override
            public DcMotorSimple.Direction getParDirection() {
                return parDirection == GoBildaPinpointDriver.EncoderDirection.FORWARD
                        ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE;
            }

            @Override
            public void setPerpDirection(@NonNull DcMotorSimple.Direction direction) {
                perpDirection = direction == DcMotorSimple.Direction.FORWARD
                        ? GoBildaPinpointDriver.EncoderDirection.FORWARD
                        : GoBildaPinpointDriver.EncoderDirection.REVERSED;
                driver.setEncoderDirections(parDirection, perpDirection);
            }

            @Override
            public DcMotorSimple.Direction getPerpDirection() {
                return perpDirection == GoBildaPinpointDriver.EncoderDirection.FORWARD
                        ? DcMotorSimple.Direction.FORWARD : DcMotorSimple.Direction.REVERSE;
            }
        };
    }
}
