package org.firstinspires.ftc.teamcode.Autonomous;

import android.graphics.Bitmap;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.Hardware.BaseRRHardware;
import org.firstinspires.ftc.teamcode.Hardware.RRCoachBotHardware;
import org.firstinspires.ftc.teamcode.Hardware.RoverRuckusBotHardware;
import org.firstinspires.ftc.teamcode.Utility.BitmapUtilities;
import org.firstinspires.ftc.teamcode.Utility.FileUtilities;
import org.firstinspires.ftc.teamcode.Utility.PolarCoord;
import org.firstinspires.ftc.teamcode.Utility.RoverRuckusUtilities;
import org.firstinspires.ftc.teamcode.Utility.VuforiaUtilities;

import static org.firstinspires.ftc.teamcode.Autonomous.BaseRoverRuckusAuto.GoldPosition.*;
import static org.firstinspires.ftc.teamcode.Autonomous.BaseRoverRuckusAuto.StartPosition.BLUE_CRATER;
import static org.firstinspires.ftc.teamcode.Autonomous.BaseRoverRuckusAuto.StartPosition.BLUE_DEPOT;
import static org.firstinspires.ftc.teamcode.Autonomous.BaseRoverRuckusAuto.StartPosition.RED_DEPOT;

@Disabled
@Autonomous (name = "TestAuto")
public class TestAuto extends LinearOpMode {

    RRCoachBotHardware robot = new RRCoachBotHardware();
    VuforiaLocalizer vuforia;
    float angleStart = 0;
    double xStart = 0;
    double yStart = 0;
    WebcamName webcam;
    VuforiaTrackables trackables;
    VuforiaTrackable blue;
    VuforiaTrackable red;
    VuforiaTrackable front;
    VuforiaTrackable back;
    public static final int TARGET_POSITION = -1120;
    public BaseRoverRuckusAuto.StartPosition initialPosition;
    String jewelConfigLeft = "jewelConfigLeft.txt";
    String jewelConfigMiddle = "jewelConfigMiddle.txt";
    String jewelConfigRight = "jewelConfigRight.txt";
    String jewelBitmap = "jewelBitmap.png";
    String jewelBitmapLeft = "jewelBitmapLeft.png";
    String jewelBitmapMiddle = "jewelBitmapMiddle.png";
    String jewelBitmapRight = "jewelBitmapRight.png";
    double x;
    double y;
    double z;
    float angleVu;

    public enum StartPosition {
        RED_CRATER, RED_DEPOT, BLUE_CRATER, BLUE_DEPOT;
    }

    public enum GoldPosition {

        LEFT, MIDDLE, RIGHT
    }

    @Override
    public void runOpMode() throws InterruptedException {

        robot.init(hardwareMap);

        waitForStart();
        BaseRoverRuckusAuto.GoldPosition goldPosition = LEFT;

        OpenGLMatrix location = VuforiaUtilities.getMatrix(0, 0, angleStart,
                (float) (xStart / VuforiaUtilities.MM_TO_INCHES),
                (float) (yStart / VuforiaUtilities.MM_TO_INCHES), 0);


        VectorF translation = location.getTranslation();

        Orientation orientation = Orientation.getOrientation(location,
                AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);


        x = (translation.get(0) * VuforiaUtilities.MM_TO_INCHES);
        y = (translation.get(1) * VuforiaUtilities.MM_TO_INCHES);
        z = ((translation.get(2) * VuforiaUtilities.MM_TO_INCHES));
        angleVu = orientation.thirdAngle;

        double angleImu = robot.getAngle();

        robot.offset = angleVu - angleImu;


        //step 3: go to the cheddar pivot point
        goToPosition(x, y,  24 , 0);

        while(robot.leftBack.isBusy());

        goToPosition(x,y,24,24);

        while(robot.leftBack.isBusy());
    }

    public void dropFromLander() {
        DcMotor.RunMode originalValue = robot.lift.getMode();
        robot.lift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.lift.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        robot.lift.setTargetPosition(TARGET_POSITION);
        robot.lift.setPower(1);
        while (robot.lift.isBusy()) {
            idle();
        }
        robot.lift.setMode(originalValue);
    }

    public BaseRoverRuckusAuto.GoldPosition determineGoldPosition() {

        Bitmap bitmap = BitmapUtilities.getVuforiaImage(vuforia);
        try {
            FileUtilities.writeBitmapFile(jewelBitmap, bitmap);

            FileUtilities.writeHueFile("jewelHuesBig.txt", bitmap);

            int leftHueTotal = RoverRuckusUtilities.getJewelHueCount(bitmap, jewelConfigLeft, jewelBitmapLeft, "jewelHuesLeft.txt")[0];
            int middleHueTotal = RoverRuckusUtilities.getJewelHueCount(bitmap, jewelConfigMiddle, jewelBitmapMiddle, "jewelHuesMiddle.txt")[0];
            int rightHueTotal = RoverRuckusUtilities.getJewelHueCount(bitmap, jewelConfigRight, jewelBitmapRight, "jewelHuesRight.txt")[0];

            String winnerLocation = BitmapUtilities.findWinnerLocation(leftHueTotal, middleHueTotal, rightHueTotal);
            FileUtilities.writeWinnerFile(winnerLocation, leftHueTotal, middleHueTotal, rightHueTotal);
            if (leftHueTotal > middleHueTotal && middleHueTotal > rightHueTotal) {
                return LEFT;
            } else if (rightHueTotal > middleHueTotal && rightHueTotal > leftHueTotal) {
                return BaseRoverRuckusAuto.GoldPosition.RIGHT;
            } else {
                return BaseRoverRuckusAuto.GoldPosition.MIDDLE;
            }
        } catch (Exception e) {
            telemetry.addData("ERROR WRITING TO FILE JEWEL BITMAP", e.getMessage());
            telemetry.update();
            return BaseRoverRuckusAuto.GoldPosition.MIDDLE;
        }

    }

    public PolarCoord getCornerPosition(BaseRoverRuckusAuto.StartPosition startPosition){
        switch (startPosition){
            case BLUE_CRATER:
                return new PolarCoord(54, -54 , 0);
            case BLUE_DEPOT:
                return new PolarCoord(54, 54, 0);
            case RED_DEPOT:
                return new PolarCoord(-54, -54, 0);
            case RED_CRATER:
                return new PolarCoord(-54 , 54, 0);
        }
        return null;
    }

    //Using The IMU to turn 90^
    double getPower(double absCurrent, double absGoal, double absStart) {

        double relCurrent = AngleUnit.normalizeDegrees(absCurrent - absStart);
        double relGoal = AngleUnit.normalizeDegrees(absGoal - absStart);
        return getPower( relCurrent, relGoal);

    }

    double getPower(double currentPosition, double goal) {
       /*
        If under halfway to the goal, have the robot speed up by .01 for every angle until it is
        over halfway there
         */
        if (goal > 0) {
            if (currentPosition < goal / 2) {

                return (.01 * currentPosition +  (Math.signum((currentPosition==0)? goal: currentPosition) * .1));
            } else {
// Starts to slow down by .01 per angle closer to the goal.
                return (.01 * (goal - currentPosition) + (Math.signum((currentPosition==0)? goal: currentPosition) * .1));
            }
        }
        else {
            if (currentPosition > goal/2){
                return (0.01 * currentPosition +  (Math.signum((currentPosition==0)? goal: currentPosition) * .1));
            }
            else {
                return (0.01 * (goal - currentPosition) +  (Math.signum((currentPosition==0)? goal: currentPosition) * .1));
            }
        }
    }

    public void goToPosition(double startX, double startY, double goalX, double goalY) {

        double xDiff = goalX - startX;
        double yDiff = goalY - startY;

        double angleGoal = Math.atan2(yDiff, xDiff) * (180 / Math.PI);

        double  angleImu = robot.getAngle();
        double distanceToGoal = Math.sqrt((Math.pow(yDiff, 2) + Math.pow(xDiff, 2)));

        if (distanceToGoal > 2) {
            while (Math.abs(angleGoal - angleImu) > 1) {
                angleImu = robot.getAngle();
                double power = getPower(angleImu, angleGoal, angleVu);
                robot.turn(-power);


                telemetry.addData("Goal Angle", angleGoal);
                telemetry.addData("angleGoal-angle ", AngleUnit.normalizeDegrees(angleGoal - angleImu));
                telemetry.addData("Robot Angle ", angleImu);
                //telemetry.addData("offset Angle", robot.offset);
                telemetry.addData("Power", power);
                telemetry.addData("start Angle", angleVu);
                telemetry.update();
                idle();
            }


            robot.goStraight(0);


            int encodersToGoal = (int) (VuforiaUtilities.INCHES_TO_ENCODERCOUNTS * distanceToGoal);

            telemetry.addData("distance to goal", distanceToGoal);
            telemetry.addData("encoders to goal", encodersToGoal);
            telemetry.update();

            robot.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            robot.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            robot.setTargetPosition(encodersToGoal);

            robot.goStraight(0.75);

            while (robot.isLeftBusy()) {

                telemetry.addData("Goal", String.format("%f %f", goalX, goalY));
                telemetry.addData("Start", String.format("%f %f", startX, startY));
                telemetry.addData("left Counts", robot.getLeftCurrentPosition());
                telemetry.addData("distance to goal", distanceToGoal);
                telemetry.addData("encoders to goal", encodersToGoal);
                telemetry.update();

                idle();
            }

            robot.goStraight(0);
        } else {
            telemetry.addData("too close not moving or turning", "");
            telemetry.update();
        }

    }

    public PolarCoord getGoldenPostition(BaseRoverRuckusAuto.GoldPosition goldPostition, BaseRoverRuckusAuto.StartPosition startPostition) {
        if (startPostition == BLUE_DEPOT) {
            switch (goldPostition) {
                case LEFT:
                    return new PolarCoord(8.019544399, 42.70655476
                            , 13.7994854);
                case MIDDLE:
                    return new PolarCoord(23.52207794, 23.52207794
                            , 45);
                case RIGHT:
                    return new PolarCoord(42.70655476, 8.019544399
                            , 76.2005146);
            }
        } else if (startPostition == BLUE_CRATER) {

            switch (goldPostition) {
                case LEFT:
                    return new PolarCoord(42.70655476, -8.019544399
                            , -76.2005146);
                case MIDDLE:
                    return new PolarCoord(23.52207794, -23.52207794
                            , -45);
                case RIGHT:
                    return new PolarCoord(8.019544399, -42.70655476
                            , -13.7994854
                    );
                //zero is when robot id looking at blue cripto graph
            }
        } else if (startPostition == RED_DEPOT) {

            switch (goldPostition) {
                case LEFT:
                    return new PolarCoord(-8.019544399, -42.70655476
                            , -166.2005146
                    );
                case MIDDLE:
                    return new PolarCoord(-23.52207794, -23.52207794
                            , -135);
                case RIGHT:
                    return new PolarCoord(-42.70655476, -8.019544399
                            , -103.7994854
                    );
            }
        } else {
            switch (goldPostition) {
                case LEFT:
                    return new PolarCoord(-42.70655476, 8.019544399
                            , 103.7994854);
                case MIDDLE:
                    return new PolarCoord(-23.52207794, 23.52207794
                            , 135);
                case RIGHT:
                    return new PolarCoord(-8.019544399, 42.70655476
                            , 166.2005146);
            }
        }
        return new PolarCoord(0, 0, 0);
    }

    public double getDistance(BaseRoverRuckusAuto.StartPosition startPosition, PolarCoord goal) {
        double distance = 0.0;
        if (startPosition == BLUE_DEPOT) {
            distance = Math.sqrt(Math.pow(54 - goal.x, 2) + Math.pow(54 - goal.y, 2));

        } else if (startPosition == BLUE_CRATER) {
            distance = Math.sqrt(Math.pow(54 - goal.x, 2) + Math.pow(-54 - goal.y, 2));

        } else if (startPosition == RED_DEPOT) {
            distance = Math.sqrt(Math.pow(-54 - goal.x, 2) + Math.pow(-54 - goal.y, 2));

        } else {
            distance = Math.sqrt(Math.pow(-54 - goal.x, 2) + Math.pow(54 - goal.y, 2));

        }
        return distance;
    }
}