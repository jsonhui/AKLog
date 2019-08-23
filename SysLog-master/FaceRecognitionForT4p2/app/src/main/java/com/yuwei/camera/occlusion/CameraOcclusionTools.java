package com.yuwei.camera.occlusion;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;

public class CameraOcclusionTools {
	private static final int DEFAULT_COLOR1 = -15724336;
//	private AlarmFlowDialog mAlarmFlowDialog;
	private Context mContext;

    public CameraOcclusionTools(Context mContext) {
        this.mContext = mContext;
//        mAlarmFlowDialog = new AlarmFlowDialog();
    }

    public Bitmap getSmallImage(Bitmap bp) {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	bp.compress(CompressFormat.JPEG, 80, baos);
        byte[] bytes = baos.toByteArray();
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 设置司机图片显示的宽跟高
        float hh = 270f;
        float ww = 240f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, newOpts);

        return bitmap;
    }
    
	public synchronized void GetPalette(Bitmap bp, final int channel, final ResultCallBack result) {
        Bitmap smallImage = getSmallImage(bp);
        Log.i("FaceService","channel===>"+channel);
//        if(mAlarmFlowDialog!=null){
//            mAlarmFlowDialog.createFloatMessage(mContext,smallImage);
//        }
        Palette.from(smallImage).maximumColorCount(24).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
            	Palette.Swatch s1 = palette.getVibrantSwatch();
            	Palette.Swatch s2 = palette.getDarkVibrantSwatch();
            	Palette.Swatch s3 = palette.getLightVibrantSwatch();
            	Palette.Swatch s4 = palette.getMutedSwatch();
            	Palette.Swatch s5 = palette.getDarkMutedSwatch();
            	Palette.Swatch s6 = palette.getLightMutedSwatch();
                List<ColorModel> mColorList = new ArrayList<>();
                int totalCount = 0;

                if (s1 != null) {
                    totalCount += s1.getPopulation();
                    mColorList.add(new ColorModel(s1.getRgb(), s1.getPopulation()));
                }
                if (s2 != null) {
                    totalCount += s2.getPopulation();
                    mColorList.add(new ColorModel(s2.getRgb(), s2.getPopulation()));
                }
                if (s3 != null) {
                    totalCount += s3.getPopulation();
                    mColorList.add(new ColorModel(s3.getRgb(), s3.getPopulation()));
                }
                if (s4 != null) {
                    totalCount += s4.getPopulation();
                    mColorList.add(new ColorModel(s4.getRgb(), s4.getPopulation()));
                }
                if (s5 != null) {
                    totalCount += s5.getPopulation();
                    mColorList.add(new ColorModel(s5.getRgb(), s5.getPopulation()));
                }
                if (s6 != null) {
                    totalCount += s6.getPopulation();
                    mColorList.add(new ColorModel(s6.getRgb(), s6.getPopulation()));
                }
                Comparator comp = new SortComparator();
                Collections.sort(mColorList, comp);
                for (ColorModel model : mColorList) {
                    model.total = totalCount;
                }
                Log.i("FaceService","channel="+channel+"   size="+mColorList.size()+"  count="+mColorList.get(0).count+"  color1="+mColorList.get(0).color);
                if (mColorList.size() <= 3) {
                    if(mColorList.size() > 0){
                        if(DEFAULT_COLOR1 +200 > mColorList.get(0).color && mColorList.get(0).color > DEFAULT_COLOR1 -200){
                            return;
                        }
                    }
                	if(result!=null) {
                		result.isOcclusion(true,channel);
                	}
                    return;
                }

                if (mColorList.size() == 4 || mColorList.size() == 5) {
                    int count = mColorList.get(0).count + mColorList.get(1).count;
                    if (percentage(count, totalCount)) {
                    	if(result!=null) {
                    		result.isOcclusion(true,channel);
                    	}
                        return;
                    }
                }

                if(result != null) {
                    result.isOcclusion(false,channel);
                }
            }
        });

    }
    
    public class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            ColorModel a = (ColorModel) lhs;
            ColorModel b = (ColorModel) rhs;
            return (b.count - a.count);
        }
    }
    
    public boolean percentage(int count, int total) {
        BigDecimal b1 = new BigDecimal(Integer.toString(count));
        BigDecimal b2 = new BigDecimal(Integer.toString(total));
        BigDecimal value = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
        return value.floatValue() > 0.96;
    }
    
    public interface ResultCallBack{
    	void isOcclusion(boolean isOcclusion,int channel);
    }

}
