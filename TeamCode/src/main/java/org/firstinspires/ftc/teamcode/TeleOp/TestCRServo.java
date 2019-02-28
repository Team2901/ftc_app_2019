package org.firstinspires.ftc.teamcode.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp (name = "Test CRServo" , group = "Test")
public class TestCRServo extends OpMode {

    CRServo crServo;

    @Override
    public void init() {
       crServo = hardwareMap.crservo.get("crServo");
    }

    @Override
    public void loop() {
    crServo.setPower( - gamepad1.right_stick_y);

    telemetry.addData("Joystick" , gamepad1.right_stick_y);
    telemetry.addData("MotorPower" , crServo.getPower());
    telemetry.update();
    }
}
