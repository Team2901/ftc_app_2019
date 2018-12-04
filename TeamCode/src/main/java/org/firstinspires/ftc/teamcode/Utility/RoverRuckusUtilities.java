package org.firstinspires.ftc.teamcode.Utility;

import android.graphics.Bitmap;

import java.io.IOException;
import java.util.List;

public class RoverRuckusUtilities {
    public static int getJewelHueCount (Bitmap bitmap, String configfile, String bitmapfilename, String huefilename) throws RuntimeException {
        try{
            List<String> configvalues = FileUtilities.readConfigFile(configfile);
            int sampleLeftXPct = Integer.valueOf(configvalues.get(0));
            int sampleTopYPct = Integer.valueOf(configvalues.get(1));;
            int sampleRightXPct = Integer.valueOf(configvalues.get(2));;
            int sampleBotYPct = Integer.valueOf(configvalues.get(3));;
            Bitmap babyBitmap = BitmapUtilities.getBabyBitmap (bitmap, sampleLeftXPct, sampleTopYPct, sampleRightXPct,sampleBotYPct);
            FileUtilities.saveBitmap(bitmapfilename, babyBitmap);
            FileUtilities.saveHueFile(huefilename, babyBitmap);
            int hueTotal= ColorUtilities.determineColor(babyBitmap, 25, 30);
            return hueTotal;
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
