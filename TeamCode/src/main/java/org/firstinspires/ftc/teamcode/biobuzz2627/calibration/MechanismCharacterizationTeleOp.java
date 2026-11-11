package org.firstinspires.ftc.teamcode.biobuzz2627.calibration;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.biobuzz2627.automation.AutomationConfig;

/** Low-power, deadman-controlled test for determining directions, velocity, and current limits. */
@Disabled
@TeleOp(name = "LOCKED Mechanism Characterization", group = "Calibration")
public final class MechanismCharacterizationTeleOp extends LinearOpMode {
    @Override
    public void runOpMode() {
        DcMotorEx shooter = hardwareMap.get(DcMotorEx.class, AutomationConfig.SHOOTER_NAME);
        DcMotorEx intake = hardwareMap.get(DcMotorEx.class, AutomationConfig.INTAKE_NAME);
        DcMotorEx transfer = hardwareMap.get(DcMotorEx.class, AutomationConfig.TRANSFER_NAME);
        waitForStart();

        try {
            while (opModeIsActive()) {
                double power = Math.copySign(
                        Math.min(Math.abs(CharacterizationConfig.TEST_POWER),
                                CharacterizationConfig.MAX_TEST_POWER),
                        CharacterizationConfig.TEST_POWER);
                boolean deadman = AutomationConfig.CHARACTERIZATION_ENABLED
                        && gamepad1.left_trigger > 0.9 && gamepad1.right_trigger > 0.9;
                shooter.setPower(deadman && gamepad1.x ? power : 0);
                intake.setPower(deadman && gamepad1.a ? power : 0);
                transfer.setPower(deadman && gamepad1.b ? power : 0);

                telemetry.addData("LOCK", AutomationConfig.CHARACTERIZATION_ENABLED
                        ? "hold both triggers + X/A/B" : "enable CHARACTERIZATION_ENABLED");
                telemetry.addData("Test power", power);
                report("Shooter", shooter);
                report("Intake", intake);
                report("Transfer", transfer);
                telemetry.update();
            }
        } finally {
            shooter.setPower(0);
            intake.setPower(0);
            transfer.setPower(0);
        }
    }

    private void report(String name, DcMotorEx motor) {
        telemetry.addData(name, "velocity %.1f ticks/s, current %.2f A",
                motor.getVelocity(), motor.getCurrent(CurrentUnit.AMPS));
    }
}
