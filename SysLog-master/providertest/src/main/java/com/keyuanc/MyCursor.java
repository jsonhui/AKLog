package com.keyuanc;

import android.database.AbstractCursor;
import android.util.Log;

import java.util.ArrayList;

public class MyCursor extends AbstractCursor {
    private static final String TAG = "Jason";

    private String[] columnNames;//构建cursor时必须先传入列明数组以规定列数

    /**
     * 数据区域
     */
    //所有的数据
    private ArrayList<Student> allDatas = new ArrayList<>();//在构造的时候填充数据，里层数据的size=columnNames.leng
    //当前一项的数据
    private Student oneLineData = null;//onMove时填充

    MyCursor() {
        //必须构建完整列信息
        columnNames = new String[]{"id", "name"};
    }

    /**
     * 加载我们的数据信息
     */
    void updateUserData(ArrayList<Student> data) {
        allDatas.clear();
        allDatas.addAll(data);
    }

    /**
     * 获取当前行对象，为一个oneLineDatastring[]
     */

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
        if (newPosition < 0 || newPosition >= getCount()) {
            oneLineData = null;
            return false;
        }

        int index = newPosition;
        if (index < 0 || index >= allDatas.size()) {
            return false;
        }
        oneLineData = allDatas.get(index);
        return super.onMove(oldPosition, newPosition);
    }

    /**
     * 获取游标行数
     */
    @Override
    public int getCount() {
        return allDatas.size();
    }

    /**
     * 获取列名称
     */
    @Override
    public String[] getColumnNames() {
        return columnNames;
    }


    @Override
    public String getString(int column) {
        if (oneLineData == null) {
            return null;
        }
        return oneLineData.toString();
    }

    @Override
    public int getInt(int column) {
        Object value = getString(column);
        try {
            return value != null ? ((Number) value).intValue() : null;
        } catch (ClassCastException e) {
            if (value instanceof CharSequence) {
                try {
                    return Integer.valueOf(value.toString().split(",")[0]);
                } catch (NumberFormatException e2) {
                    Log.e(TAG, "Cannotparse int value for " + value + "at key " + column);
                    return 0;
                }
            } else {
                Log.e(TAG, "Cannotcast value for " + column + "to a int: " + value, e);
                return 0;
            }
        }
    }

    /**
     * 以下参考getInt(int column)
     */

    @Override
    public short getShort(int column) {
        return 0;
    }

    @Override
    public long getLong(int column) {
        return 0;
    }

    @Override
    public float getFloat(int column) {
        return 0;
    }

    @Override
    public double getDouble(int column) {
        return 0;
    }

    @Override
    public boolean isNull(int column) {
        return false;
    }
}
