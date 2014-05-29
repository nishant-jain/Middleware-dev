package com.middleware.pubsubclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TheMainController extends Activity
	{

		Intent intent1 = null;
		PowerManager powerManager;
		PowerManager.WakeLock wakeLock;

		@Override
		protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.layout_controller);
				powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
				wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
						"SERVICE_SENSORS");
				Button start = (Button) findViewById(R.id.serviceButton);
				start.setOnClickListener(startListener);
				Button stop = (Button) findViewById(R.id.cancelButton);
				stop.setOnClickListener(stopListener);

				intent1 = new Intent(TheMainController.this, AccReadings.class);

			}

		private final OnClickListener startListener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{

						wakeLock.acquire();

						startService(intent1);

					}
			};

		private final OnClickListener stopListener = new OnClickListener()
			{
				@Override
				public void onClick(View v)
					{
						wakeLock.release();
						stopService(intent1);

						Toast.makeText(getApplicationContext(), "Services destroyed.",
								Toast.LENGTH_SHORT).show();
					}
			};

	}
