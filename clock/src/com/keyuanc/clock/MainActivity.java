package com.keyuanc.clock;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	private ScreenSaverClock screenSaverClock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		screenSaverClock = (ScreenSaverClock) findViewById(R.id.clock);
	}

	@Override
	protected void onResume() {
		super.onResume();
		screenSaverClock.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		screenSaverClock.stop();
	}
}
