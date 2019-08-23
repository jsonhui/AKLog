package com.yuwei.face.register;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yuwei.face.db.DataBaseManager;
import com.yuwei.face.register.R;
import com.yuwei.face.register.view.CertificateMainView;
import com.yuwei.sdk.YwSdkManager;
import com.yuwei.sdk.entity.DriverInfo;
import com.yuwei.sdk.event.EventTerminalParamC2;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class RfidDetectActivity extends Activity implements CertificateMainView.GoToRegisterCallBack {
    private ImageView mRfidNotifyIv;
    private CertificateMainView  mCertificateMainView;
    private String mDriverCode;
    private DataBaseManager mDataBaseManager;
    private String rfidDriverCode = "";
    private YwSdkManager mYwSdkManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_rfid_detect);
        mRfidNotifyIv = (ImageView) findViewById(R.id.rfid_notify_bg);
        mCertificateMainView = (CertificateMainView)findViewById(R.id.certifi_view);
        mCertificateMainView.setmGoToRegisterCallBack(this);
        mDataBaseManager = DataBaseManager.getInstance();
        EventBus.getDefault().register(this);

        mRfidNotifyIv.setVisibility(View.GONE);
        mCertificateMainView.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MainThread)
    public void onMessageEvent(EventTerminalParamC2 event) {
        if (event == null)
            return;
        if(event.getDriverInfo() == null)
            return;
        if (event.getDriverInfo().getDriverCode() == null)
            return;
        String tmp = event.getDriverInfo().getDriverCode();
        if(rfidDriverCode.equals(tmp)) {
            return;
        }else{
            rfidDriverCode = tmp;
        }
        mDriverCode = event.getDriverInfo().getDriverCode();
        Log.i("RecognizeActivity","::::::::::::" + mDriverCode);
        //RFID刷卡后隐藏提示主界面，显示司机信息界面
        mRfidNotifyIv.setVisibility(View.GONE);
        mCertificateMainView.setVisibility(View.VISIBLE);
        mCertificateMainView.showDriverInfo(event.getDriverInfo());
//        if(mDataBaseManager != null){
//            //判断RFID卡对应的司机编号存不存在，如果存在则从数据查询司机完整信息，进行显示
//            //如果不存在，将RFID卡司机信息插入数据库
//            if(mDataBaseManager.isExistCertificate(mDriverCode)) {
//                DriverInfo driverInfo = mDataBaseManager.getDriverInfo(mDriverCode);
//                if (driverInfo != null)
//                    mCertificateMainView.showDriverInfo(driverInfo);
//            }else{
//                mDataBaseManager.insertDriver(event.getDriverInfo());
//                mCertificateMainView.showDriverInfo(event.getDriverInfo());
//            }
//        }
    }

    @Override
    public void goToFaceRegister() {
        Intent intent = new Intent(RfidDetectActivity.this,NewCameraRegisterActivity.class);
        if (mDriverCode != null)
            intent.putExtra("driver_code",mDriverCode);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mYwSdkManager == null)
            mYwSdkManager = YwSdkManager.getInstants(this);
        //Settings.System.putInt(this.getContentResolver(),Constant.FACE_REGISTER_ACTION,Constant.FACE_REGISTER_STATUS_REGISTER);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
