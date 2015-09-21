package me.dt2dev.wheelview.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

/**
 * Created by darktiny on 9/21/15.
 */
public final class CompatUtil {

    private CompatUtil() {
    }

    public static int getColor(Context context, int colorRes) {
        Resources resources = context.getResources();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return resources.getColor(colorRes);
        } else {
            return resources.getColor(colorRes, null);
        }
    }
}
