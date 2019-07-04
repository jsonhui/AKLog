/* SysLog - A simple logging tool
 * Copyright (C) 2013-2016  Scott Warner <Tortel1210@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.keyuanc.logtest;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

import eu.chainfire.libsuperuser.Shell;

/**
 * General utilities and the main run command code
 */
public class Utils {

    private static final int MB_TO_BYTE = 1048576;
    /**
     * Minimum amount of free space needed to not throw a LowSpaceException.
     * This is based on the average size of my runs (~5.5MB)
     */
    public static final double MIN_FREE_SPACE = 6;
    private static final String TAG = "Utils";

    /**
     * Gets the free space of the primary storage, in MB
     *
     * @return the space
     */
    @SuppressWarnings("deprecation")
    public static double getStorageFreeSpace() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        return Math.floor(sdAvailSize / MB_TO_BYTE);
    }

    public static class ClearLogcatTaskLog extends AsyncTask<Void, Void, Void> {

        private final String path_0;

        ClearLogcatTaskLog(String path) {
            path_0 = path;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String path = path_0 + "/AKLog/";
            File file = new File(path);
            if (file.exists() && file.listFiles() != null && file.listFiles().length > 0) {
                ArrayList<String> datas = new ArrayList<>();
                File[] array = file.listFiles();
                for (File file1 : array) {
                    datas.add(file1.getName());
                }
                Collections.sort(datas);//排序删除第一个
                Shell.SH.run("rm -rf " + path + datas.get(0));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void param) {
        }

    }
}
