package com.yuwei.face.db;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.yuwei.sdk.certificate.DataBase;
import com.yuwei.sdk.certificate.PndDBDefine;
import com.yuwei.sdk.entity.DriverInfo;

/**
 * 人脸识别数据库管理
 */
public class DataBaseManager {
    private Context mContext;
    private static DataBaseManager dataBaseManager;
    private final String TABLE_CERTIFICATEINFO_NAME = "tb_Certificate_record";
    private final String DRIVER_AUTHORITY = "com.android.pnd.driverlisence";
    private final Uri DRIVER_CONTENT_URI = Uri.parse("content://" + DRIVER_AUTHORITY);
    private Uri DRIVER_LISENCE_URI = Uri.withAppendedPath(DRIVER_CONTENT_URI,TABLE_CERTIFICATEINFO_NAME);
    private DataBaseManager(Context context){
        this.mContext = context;
    }
    public static DataBaseManager getInstance(Context context){
        if (dataBaseManager == null) {
            synchronized (DataBaseManager.class) {
                if (dataBaseManager == null)
                     dataBaseManager = new DataBaseManager(context);
                DataBase.getInstance(context);
            }
        }
        return dataBaseManager;
    }

    public static DataBaseManager getInstance(){
        return dataBaseManager;
    }
    public DriverInfo getDriverInfo(String driver_code){
        if(mContext == null || driver_code == null)
            return null;
        Cursor cursor =  mContext.getContentResolver().query(DRIVER_LISENCE_URI, null, "Drivercode='"+ driver_code + "'", null, null);
        if(cursor == null)
            return null;
        if (cursor.getCount() == 0)
            return null;
        DriverInfo driver = null;
        if(cursor.moveToFirst()){
            driver = new DriverInfo();
            driver.setUpdateFlag(cursor.getInt(cursor.getColumnIndex("UpdateFlag")));
            driver.setDriverName(cursor.getString(cursor.getColumnIndex("DriverName")));
            driver.setDriverCode(cursor.getString(cursor.getColumnIndex("Drivercode")));
            driver.setDriverId(cursor.getString(cursor.getColumnIndex("DriverID")));
            driver.setDriverVehCertificate(cursor.getString(cursor.getColumnIndex("CertificateNo")));
            driver.setDriverVehPlate(cursor.getString(cursor.getColumnIndex("VehPlate")));
            driver.setDriverCompany(cursor.getString(cursor.getColumnIndex("Company")));
            driver.setDriverCertificateCompany(cursor.getString(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_JIANDU_ORG)));
            driver.setDriverServiceLevel(cursor.getInt(cursor.getColumnIndex("ServiceLevel")));
            driver.setDriverCertificateDate(cursor.getString(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_CERTIFICATEDATE_BEGIN)));
            driver.setDriverExpire(cursor.getString(cursor.getColumnIndex("CertificateDateEnd")));
            driver.setDriverTitle(cursor.getString(cursor.getColumnIndex("Title")));
            driver.setDriverPhotoName(cursor.getString(cursor.getColumnIndex("PhotoName")));
            driver.setDriverTeleCod(cursor.getString(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_JIANDU_TELCODE)));
            driver.setBaoliu(cursor.getString(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_BAOLIU)));
            driver.setZhengban(cursor.getInt(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_ZHENGBAN_LABEL)));
            driver.setJiaoyan(cursor.getInt(cursor.getColumnIndex(PndDBDefine.DriverLisenceColumns.TABLE_JIAOYAN)));
        }
        cursor.close();
        return driver;
    }

    public void insertDriver(DriverInfo driver){
        try {
            DataBase.getDriverLisenceDB().insertCertificateData(" ",
                    driver.getUpdateFlag(), " ",
                    driver.getDriverName(), " ",
                    driver.getDriverCode(),
                    driver.getDriverVehCertificate(),
                    driver.getDriverCertificateDate(),
                    driver.getDriverExpire(),
                    driver.getDriverId(),
                    driver.getDriverVehPlate(),
                    driver.getDriverCompany(),
                    driver.getDriverCertificateCompany(),
                    driver.getDriverTeleCod(),
                    driver.getDriverServiceLevel(),
                    driver.getDriverTitle(), " ", "", " ",
                    " ",
                    driver.getZhengban(),
                    driver.getJiaoyan());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    //判断从业资格证是否存在
    public boolean isExistCertificate(String _id){
        if(mContext == null || _id == null)
            return false;
        Cursor cursor =  mContext.getContentResolver().query(DRIVER_LISENCE_URI, null, "Drivercode='"+ _id + "'", null, null);
        if(cursor.getCount() != 0)
            return true;
        else
            return false;
    }
}
