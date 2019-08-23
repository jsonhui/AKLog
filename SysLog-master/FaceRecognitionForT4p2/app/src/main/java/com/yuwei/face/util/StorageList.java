package com.yuwei.face.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

public class StorageList {

	public static final String TAG = "StorageList";
    private StorageManager mStorageManager;
    private static StorageList mList;
    private String[] mPath;
    private Method mMethodGetPaths;
    private static String INTERNAL_STORAGE_DIRECTORY = ""; // mnt/sdcard
    private static String EXTERNAL_STORAGE_DIRECTORY = ""; // mnt/extsd
    private static String USB_STORAGE_DIRECTORY = ""; // U盘/mnt/usbhost/Storage01

    public static StorageList getInstance(Context context) {
        if (mList == null)
            mList = new StorageList(context.getApplicationContext());
        mList.getVolumePath(context.getApplicationContext());
        return mList;
    }
    
    private StorageList(Context context) {
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
			mMethodGetPaths = mStorageManager.getClass()
					.getMethod("getVolumePaths");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
    }
    
    private void getVolumePath(Context context) {
        if (context == null)
            return;
        INTERNAL_STORAGE_DIRECTORY = "";
        EXTERNAL_STORAGE_DIRECTORY = "";
        USB_STORAGE_DIRECTORY = "";
        
        try {
			mPath = (String[]) mMethodGetPaths.invoke(mStorageManager);
			Log.i("StorageList", "mPath.length:"+mPath.length);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//        mPath = mStorageManager.getVolumePaths();
        if (mPath.length > 0) { // t6v3二代 内置sd卡 /storage/emulated/0
            INTERNAL_STORAGE_DIRECTORY = mPath[0];
            Log.i("StorageList", "mPath[0]"+mPath[0]);
        }
		if (mPath.length > 1) { // t4二代 外置sd卡在第二个位置
			EXTERNAL_STORAGE_DIRECTORY = mPath[1];
//			if (mPath[1].endsWith("card1") || mPath[1].endsWith("0000")) {
//				EXTERNAL_STORAGE_DIRECTORY = mPath[1];
//			} else {
//				USB_STORAGE_DIRECTORY = mPath[1];
//			}
			Log.i("StorageList", "mPath[1]"+mPath[1]);
		}
//        if (mPath.length > 2) { // t6v3二代 外置2个sd卡 /storage/card & /storage/extsd
//			if (mPath[2].endsWith("card1")) {
//				EXTERNAL_STORAGE_DIRECTORY = mPath[2];
//			} else {
//				USB_STORAGE_DIRECTORY = mPath[2];
//			}
//			Log.i("StorageList", "mPath[2]"+mPath[2]);
//        }
    }

    public String[] getVolumePaths() {
        return mPath;
    }

    @SuppressWarnings("deprecation")
	public boolean getInternalStorageState() {
        if (TextUtils.isEmpty(INTERNAL_STORAGE_DIRECTORY)) {
            return false;
        }
        try {
            return "mounted".equals(Environment.getStorageState(new File(
                    INTERNAL_STORAGE_DIRECTORY)));
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
	public boolean getExternalStorageState() {
        if (TextUtils.isEmpty(EXTERNAL_STORAGE_DIRECTORY)) {
            return false;
        }
        try {
            return "mounted".equals(Environment.getStorageState(new File(
                    EXTERNAL_STORAGE_DIRECTORY)));
        } catch (Exception rex) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
	public boolean getUSBStorageState() {
        if (TextUtils.isEmpty(USB_STORAGE_DIRECTORY)) {
            return false;
        }
        try {
            return "mounted".equals(Environment.getStorageState(new File(
                    USB_STORAGE_DIRECTORY)));
        } catch (Exception rex) {
            return false;
        }
    }

    public String getInternalStorage() {
        return INTERNAL_STORAGE_DIRECTORY;
    }

    public String getExternalStorage() {
        return EXTERNAL_STORAGE_DIRECTORY;
    }

    public String getUSBStorage() {
        return USB_STORAGE_DIRECTORY;
    }

}
