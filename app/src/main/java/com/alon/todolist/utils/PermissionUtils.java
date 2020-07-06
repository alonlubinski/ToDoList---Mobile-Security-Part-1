package com.alon.todolist.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class PermissionUtils {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean neverAskAgainSelected(final Activity activity, final String permission) {
        final boolean prevShouldShowStatus = getRationaleDisplayStatus(activity,permission);
        final boolean currShouldShowStatus = activity.shouldShowRequestPermissionRationale(permission);
        return prevShouldShowStatus != currShouldShowStatus;
    }

    public static void setShouldShowStatus(final Context context, final String permission) {
        MySP.getInstance().putBoolean(permission, true);
    }
    public static boolean getRationaleDisplayStatus(final Context context, final String permission) {
        return MySP.getInstance().getBoolean(permission, false);
    }
}
