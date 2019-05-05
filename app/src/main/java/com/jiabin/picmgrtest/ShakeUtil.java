package com.jiabin.picmgrtest;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

public class ShakeUtil {

    public static void vibrator(Context context, long duration) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(duration);
    }
}
