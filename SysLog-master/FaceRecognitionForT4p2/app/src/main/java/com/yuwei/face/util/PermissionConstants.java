package com.yuwei.face.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PermissionConstants {

    public static final String CALENDAR = "android.permission-group.CALENDAR";
    public static final String CAMERA = "android.permission-group.CAMERA";
    public static final String CONTACTS = "android.permission-group.CONTACTS";
    public static final String LOCATION = "android.permission-group.LOCATION";
    public static final String MICROPHONE = "android.permission-group.MICROPHONE";
    public static final String PHONE = "android.permission-group.PHONE";
    public static final String SENSORS = "android.permission-group.SENSORS";
    public static final String SMS = "android.permission-group.SMS";
    public static final String STORAGE = "android.permission-group.STORAGE";
    private static final String[] GROUP_CALENDAR = new String[]{"android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR"};
    private static final String[] GROUP_CAMERA = new String[]{"android.permission.CAMERA"};
    private static final String[] GROUP_CONTACTS = new String[]{"android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS", "android.permission.GET_ACCOUNTS"};
    private static final String[] GROUP_LOCATION = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private static final String[] GROUP_MICROPHONE = new String[]{"android.permission.RECORD_AUDIO"};
    private static final String[] GROUP_PHONE = new String[]{"android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS", "android.permission.CALL_PHONE", "android.permission.ANSWER_PHONE_CALLS", "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG", "com.android.voicemail.permission.ADD_VOICEMAIL", "android.permission.USE_SIP", "android.permission.PROCESS_OUTGOING_CALLS"};
    private static final String[] GROUP_SENSORS = new String[]{"android.permission.BODY_SENSORS"};
    private static final String[] GROUP_SMS = new String[]{"android.permission.SEND_SMS", "android.permission.RECEIVE_SMS", "android.permission.READ_SMS", "android.permission.RECEIVE_WAP_PUSH", "android.permission.RECEIVE_MMS"};
    private static final String[] GROUP_STORAGE = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

    public PermissionConstants() {
    }

    public static String[] getPermissions(String var0) {
        byte var2 = -1;
        switch(var0.hashCode()) {
            case -1639857183:
                if (var0.equals("android.permission-group.CONTACTS")) {
                    var2 = 2;
                }
                break;
            case -1410061184:
                if (var0.equals("android.permission-group.PHONE")) {
                    var2 = 5;
                }
                break;
            case -1250730292:
                if (var0.equals("android.permission-group.CALENDAR")) {
                    var2 = 0;
                }
                break;
            case -1140935117:
                if (var0.equals("android.permission-group.CAMERA")) {
                    var2 = 1;
                }
                break;
            case 421761675:
                if (var0.equals("android.permission-group.SENSORS")) {
                    var2 = 6;
                }
                break;
            case 828638019:
                if (var0.equals("android.permission-group.LOCATION")) {
                    var2 = 3;
                }
                break;
            case 852078861:
                if (var0.equals("android.permission-group.STORAGE")) {
                    var2 = 8;
                }
                break;
            case 1581272376:
                if (var0.equals("android.permission-group.MICROPHONE")) {
                    var2 = 4;
                }
                break;
            case 1795181803:
                if (var0.equals("android.permission-group.SMS")) {
                    var2 = 7;
                }
        }

        switch(var2) {
            case 0:
                return GROUP_CALENDAR;
            case 1:
                return GROUP_CAMERA;
            case 2:
                return GROUP_CONTACTS;
            case 3:
                return GROUP_LOCATION;
            case 4:
                return GROUP_MICROPHONE;
            case 5:
                return GROUP_PHONE;
            case 6:
                return GROUP_SENSORS;
            case 7:
                return GROUP_SMS;
            case 8:
                return GROUP_STORAGE;
            default:
                return new String[]{var0};
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Permission {
    }
}
