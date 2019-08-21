package com.arandroid.univoice.utils;

import android.content.Context;
import android.util.TypedValue;

public class UIUtils {

    public static int pixel(Context context, int dp){
        final float scale = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return  (int) (scale);
    }
}
