package com.keyuanc.logtest;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import eu.chainfire.libsuperuser.Shell;

public class OperationLog implements OperationLogImp {
    private String TAG = "OperationLog";
    private volatile static OperationLog instance = null;
    private final String path;//跟目录
    private Timer mTimer;//定时器
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);//日期格式
    private long delay = 1000;
    private long period = 10 * 60 * 1000;
    private TimerTask mTask = new TimerTask() {
        @Override
        public void run() {
            runCommand();
            Log.v(TAG, "收集日志");
        }
    };

    private void clearLog() {//清空部分日志
        new Utils.ClearLogcatTaskLog(this.path).execute();
    }


    private OperationLog() {
        path = Environment.getExternalStorageDirectory().getPath();
    }

    static OperationLog getInstance() {
        if (instance == null) {
            synchronized (OperationLog.class) {
                if (instance == null) {
                    instance = new OperationLog();
                }
            }
        }
        return instance;
    }

    @Override
    public void init() {
        mTimer = new Timer();
        mTimer.schedule(mTask, delay, period);
    }

    @Override
    public void unInit() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void runCommand() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            double freeSpace = Utils.getStorageFreeSpace();
            if (freeSpace < Utils.MIN_FREE_SPACE) {
                clearLog();//清空部分日志
            }
            List<String> commands = new LinkedList<>();
            List<String> files = new LinkedList<>();
            Log.e(TAG, path);
            String fileLogPath = path + "/AKLog/" + sdf.format(new Date()) + "/";
            Log.e(TAG, fileLogPath);
            File outPath = new File(fileLogPath);
            if (!outPath.mkdirs() && !outPath.isDirectory()) {
                Log.e(TAG, "创建" + outPath.getAbsolutePath() + "失败");
            }
            commands.add("logcat -v time -d");
            files.add(outPath.getAbsolutePath() + "/logcat.log");
            commands.add("dmesg");
            files.add(outPath.getAbsolutePath() + "/dmesg.log");
            Shell.Builder builder = new Shell.Builder();
            builder.useSH();
            final Shell.Interactive shell = builder.open();
            runComamnds(shell, commands, files);
        }
    }

    /**
     * Called when the commands are complete
     */
    private void commandsComplete(boolean success) {
        if (!success) {
            return;
        }
    }


    /**
     * Run the commands provided, and write the output to the path provided
     */
    private void runComamnds(final Shell.Interactive shell, final List<String> commands, final List<String> outputFiles) {
        if (commands.size() == 0) {
            commandsComplete(true);
            return;
        }
        try {
            String command = commands.remove(0);
            String outputFileName = outputFiles.remove(0);
            File outputFile = new File(outputFileName);
            final BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));

            shell.addCommand(command, 0, new Shell.OnCommandLineListener() {
                @Override
                public void onCommandResult(int commandCode, int exitCode) {
                    try {
                        output.flush();
                        output.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Exception closing writer", e);
                    }
                    runComamnds(shell, commands, outputFiles);
                }

                @Override
                public void onLine(String line) {
                    try {
                        output.write(line + "\n");
                    } catch (IOException e) {
                        Log.e("Exception writing line", e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            commandsComplete(false);
        }
    }
}
