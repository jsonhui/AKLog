package com.yuwei.face.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {

    @SuppressLint({"StaticFieldLeak"})
    private static Application sApplication;
    private static final LinkedList<Activity> ACTIVITY_LIST = new LinkedList();
    private static Application.ActivityLifecycleCallbacks mCallbacks = new Application.ActivityLifecycleCallbacks() {
        public void onActivityCreated(Activity var1, Bundle var2) {
            Utils.setTopActivity(var1);
        }

        public void onActivityStarted(Activity var1) {
            Utils.setTopActivity(var1);
        }

        public void onActivityResumed(Activity var1) {
            Utils.setTopActivity(var1);
        }

        public void onActivityPaused(Activity var1) {
        }

        public void onActivityStopped(Activity var1) {
        }

        public void onActivitySaveInstanceState(Activity var1, Bundle var2) {
        }

        public void onActivityDestroyed(Activity var1) {
            Utils.ACTIVITY_LIST.remove(var1);
        }
    };

    private Utils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void init(@NonNull Context var0) {
        init((Application)var0.getApplicationContext());
    }

    public static void init(@NonNull Application var0) {
        if (sApplication == null) {
            sApplication = var0;
            sApplication.registerActivityLifecycleCallbacks(mCallbacks);
        }

    }

    public static Application getApp() {
        if (sApplication != null) {
            return sApplication;
        } else {
            try {
                Class var0 = Class.forName("android.app.ActivityThread");
                Object var1 = var0.getMethod("currentActivityThread").invoke((Object)null);
                Object var2 = var0.getMethod("getApplication").invoke(var1);
                if (var2 == null) {
                    throw new NullPointerException("u should initData first");
                }

                init((Application)var2);
                return sApplication;
            } catch (NoSuchMethodException var3) {
                var3.printStackTrace();
            } catch (IllegalAccessException var4) {
                var4.printStackTrace();
            } catch (InvocationTargetException var5) {
                var5.printStackTrace();
            } catch (ClassNotFoundException var6) {
                var6.printStackTrace();
            }

            throw new NullPointerException("u should initData first");
        }
    }

    private static void setTopActivity(Activity var0) {
        if (var0.getClass() != PermissionUtils.PermissionActivity.class) {
            if (ACTIVITY_LIST.contains(var0)) {
                if (!((Activity)ACTIVITY_LIST.getLast()).equals(var0)) {
                    ACTIVITY_LIST.remove(var0);
                    ACTIVITY_LIST.addLast(var0);
                }
            } else {
                ACTIVITY_LIST.addLast(var0);
            }

        }
    }

    static LinkedList<Activity> getActivityList() {
        return ACTIVITY_LIST;
    }

    static Context getTopActivityOrApp() {
        if (isAppForeground()) {
            Activity var0 = getTopActivity();
            return (Context)(var0 == null ? getApp() : var0);
        } else {
            return getApp();
        }
    }

    static boolean isAppForeground() {
        @SuppressLint("WrongConstant") ActivityManager var0 = (ActivityManager)getApp().getSystemService("activity");
        if (var0 == null) {
            return false;
        } else {
            List var1 = var0.getRunningAppProcesses();
            if (var1 != null && var1.size() != 0) {
                Iterator var2 = var1.iterator();

                ActivityManager.RunningAppProcessInfo var3;
                do {
                    if (!var2.hasNext()) {
                        return false;
                    }

                    var3 = (ActivityManager.RunningAppProcessInfo)var2.next();
                } while(var3.importance != 100);

                return var3.processName.equals(getApp().getPackageName());
            } else {
                return false;
            }
        }
    }

    static Activity getTopActivity() {
        if (!ACTIVITY_LIST.isEmpty()) {
            Activity var0 = (Activity)ACTIVITY_LIST.getLast();
            if (var0 != null) {
                return var0;
            }
        }

        try {
            Class var15 = Class.forName("android.app.ActivityThread");
            Object var1 = var15.getMethod("currentActivityThread").invoke((Object)null);
            Field var2 = var15.getDeclaredField("mActivities");
            var2.setAccessible(true);
            Map var3 = (Map)var2.get(var1);
            if (var3 == null) {
                return null;
            }

            Iterator var4 = var3.values().iterator();

            while(var4.hasNext()) {
                Object var5 = var4.next();
                Class var6 = var5.getClass();
                Field var7 = var6.getDeclaredField("paused");
                var7.setAccessible(true);
                if (!var7.getBoolean(var5)) {
                    Field var8 = var6.getDeclaredField("activity");
                    var8.setAccessible(true);
                    Activity var9 = (Activity)var8.get(var5);
                    setTopActivity(var9);
                    return var9;
                }
            }
        } catch (ClassNotFoundException var10) {
            var10.printStackTrace();
        } catch (IllegalAccessException var11) {
            var11.printStackTrace();
        } catch (InvocationTargetException var12) {
            var12.printStackTrace();
        } catch (NoSuchMethodException var13) {
            var13.printStackTrace();
        } catch (NoSuchFieldException var14) {
            var14.printStackTrace();
        }

        return null;
    }
}
