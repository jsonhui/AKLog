package com.keyuanc.logtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button run_log, run_log_u;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        run_log = findViewById(R.id.run_log);
        run_log_u = findViewById(R.id.run_log_u);
        run_log.setOnClickListener(this);
        run_log_u.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.run_log:
                OperationLog.getInstance().init();
                break;
            case R.id.run_log_u:
                OperationLog.getInstance().unInit();
                break;
            default:
                break;
        }
    }
}
