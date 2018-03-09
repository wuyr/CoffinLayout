package com.test.viewtest.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by wuyr on 2/21/16 4:00 PM.
 */
/*
 * 状态栏沉浸工具类
 * */
public class ImmerseUtil {

    private static final String STATUS_BAR_HEIGHT = "status_bar_height",
            NAVIGATION_BAT_HEIGHT = "navigation_bar_height",
            DIMEN = "dimen", ANDROID = "android";

    public static boolean isHasNavigationBar(Context context) {
        boolean isHasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", ANDROID);
        if (id > 0)
            isHasNavigationBar = rs.getBoolean(id);
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                isHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                isHasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(ImmerseUtil.class.getSimpleName(), e.toString(), e);
        }
        return isHasNavigationBar;
    }

    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier(NAVIGATION_BAT_HEIGHT, DIMEN, ANDROID);
        if (id > 0 && isHasNavigationBar(context))
            navigationBarHeight = rs.getDimensionPixelSize(id);
        return navigationBarHeight;
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        try {
            int resourceId = context.getResources().getIdentifier(STATUS_BAR_HEIGHT, DIMEN, ANDROID);
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean isAboveKITKAT() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
