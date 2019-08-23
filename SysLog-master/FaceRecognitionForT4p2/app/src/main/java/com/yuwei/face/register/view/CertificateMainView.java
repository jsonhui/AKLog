package com.yuwei.face.register.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import com.yuwei.face.R;
import com.yuwei.face.util.Constant;
import com.yuwei.face.util.YuweiFaceUtil;
import com.yuwei.sdk.entity.DriverInfo;

public class CertificateMainView extends RelativeLayout{
    private ImageView mDriverPhoto,mDriverLevel;
    private TextView mDriverName,mDriverCerNo,mDriverPlateNo,mDriverValid,mPhone;
    private GoToRegisterCallBack mGoToRegisterCallBack;
    /**
     * 服务等级:1,2,3,4,5星
     */
    private final int LEVEL_ONE = 1;
    private final int LEVEL_TWO = 2;
    private final int LEVEL_THREE = 3;
    private final int LEVEL_FOUR = 4;
    private final int LEVEL_FIVE = 5;
    
    private Context mContext;

    public CertificateMainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView(context);
    }
    
    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.certificate_new_view, this);
        mDriverPhoto = (ImageView) findViewById(R.id.driver_photo);
        mDriverLevel = (ImageView) findViewById(R.id.cer_level);
        mDriverName = (TextView) findViewById(R.id.driver_name);
        mDriverCerNo = (TextView) findViewById(R.id.cer_number);
        mDriverPlateNo = (TextView) findViewById(R.id.cer_plate_number);
        mDriverValid = (TextView) findViewById(R.id.cer_validity_date);
        mPhone = (TextView) findViewById(R.id.cer_telephony);
        findViewById(R.id.register_face_btn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mGoToRegisterCallBack != null)
                    mGoToRegisterCallBack.goToFaceRegister();
            }
        });
//        AssetManager assets = context.getAssets();
//        Typeface font = Typeface.createFromAsset(assets, "digital_7mono.ttf");
    }

    /**
     * 显示名称
     * @param name
     */
    public void showDriverName(String name){
        if(name != null){
            mDriverName.setText(name);
        }
    }
    
    /**
     * 显示从业资格证
     * @param info
     */
    public void showDriverInfo(DriverInfo info){
        if(info != null){
            mDriverName.setText(info.getDriverName());
            mDriverCerNo.setText(info.getDriverVehCertificate());
            mDriverPlateNo.setText(info.getDriverVehPlate());
            mDriverValid.setText(info.getDriverCertificateDate());
            mPhone.setText(info.getDriverTeleCod());
            updateLevel(info.getDriverServiceLevel());

            String path = "";
            if(info.getDriverPhotoName() != null){
                if(info.getDriverPhotoName().endsWith(".jpg"))
                    path = Constant.CERTI_Dir+info.getDriverPhotoName();
                else
                    path = Constant.CERTI_Dir+info.getDriverPhotoName()+".jpg";
                // 使用异步任务来加载司机图片
                new LoadDriverImageAsyncTask(mDriverPhoto, path).execute();
            }
        }
    }
    
    /**
     * 情空从业资格证
     */
    public void clearDriverInfo(){
        mDriverName.setText("");
        mDriverCerNo.setText("");
        mDriverPlateNo.setText("");
        mDriverValid.setText("");
        mPhone.setText("");
        updateLevel(-1);
        mDriverPhoto.setBackgroundResource(R.drawable.default_driver_bg);
    }

    //后台更新图片
    private class LoadDriverImageAsyncTask extends AsyncTask<Void, Void, Void> {
        private ImageView imageview;
        private String path;
        private Bitmap bm;
        public LoadDriverImageAsyncTask(ImageView _imageview,String _path){
            this.imageview = _imageview;
            this.path = _path;
        }
        @Override
        protected Void doInBackground(Void... params) {
            File photo = new File(path);
            if(photo.exists()){
                try {
                    bm = YuweiFaceUtil.getimage(path,false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(bm != null){
                imageview.setImageBitmap(null);//设置背景为空，防止缓存导致默认司机图片和真实司机图片重叠
                imageview.setImageBitmap(bm);
            }else{
                imageview.setBackgroundResource(R.drawable.default_driver_bg);
            }
            imageview.invalidate();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }       
    }
    
    /**
     * 更新服务等级
     * @param level
     */
    private void updateLevel(int level){
        switch(level){
            case LEVEL_ONE:
                mDriverLevel.setBackgroundResource(R.drawable.evaluate_1);
                break;
            case LEVEL_TWO:
                mDriverLevel.setBackgroundResource(R.drawable.evaluate_2);
                break;
            case LEVEL_THREE:
                mDriverLevel.setBackgroundResource(R.drawable.evaluate_3);
                break;
            case LEVEL_FOUR:
                mDriverLevel.setBackgroundResource(R.drawable.evaluate_4);
                break;
            case LEVEL_FIVE:
                mDriverLevel.setBackgroundResource(R.drawable.evaluate_5);
                break;
            default:
                mDriverLevel.setBackgroundResource(0);
                break;
        }
    }
    public interface GoToRegisterCallBack{
        public void goToFaceRegister();
    }
    public GoToRegisterCallBack getmGoToRegisterCallBack() {
        return mGoToRegisterCallBack;
    }

    public void setmGoToRegisterCallBack(GoToRegisterCallBack mGoToRegisterCallBack) {
        this.mGoToRegisterCallBack = mGoToRegisterCallBack;
    }
}
