package com.yuwei.face.register;

import android.app.Application;
import android.content.Context;
import android.util.Log;

//import com.yuwei.face.camera.CameraManager;
import com.yuwei.face.db.DataBaseManager;

public class RegisterFaceApplication extends Application {
    public static boolean DEBUG = true;
    public static boolean INIT_STATE = false;
    public static final String EXDB = "EX";
    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this.getApplicationContext();
        DataBaseManager.getInstance(context);
//        CameraManager.getInstance(context);
//        initSDK(context);
    }

//    public static void initSDK(Context context) {
//        FaceDetectManager.setDebug(DEBUG);
//        Status status = FaceDetectManager.init(context);
//        Log.i("RecognizeActivity","-------------status----------" + status.name());
//        if (status == Status.Ok) {
//            INIT_STATE = true;
//            FaceDetectManager.initDb(EXDB);
//        } else {
//           INIT_STATE = false;
//            INIT_STATE_ERROR = status;
//        }
//    }
}
