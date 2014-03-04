package com.middleware.pubsubclient;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

public class PublishQuery extends Activity {

	private Spinner deviceCount;
	private EditText minCount;
	private EditText max;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_publish_query);
		deviceCount = (Spinner)findViewById(R.id.Spinner02);
		minCount = (EditText)findViewById(R.id.editText6);
		max = (EditText)findViewById(R.id.EditText06);
		deviceCount.setOnItemSelectedListener(new OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int pos, long arg3) {
				if(pos==1)//Exact
				{
					minCount.setHint("Count");
					max.setVisibility(View.INVISIBLE);
				}
				else//Range
				{
					minCount.setHint("Min");
					max.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
			
		});
	}
	
	public void onDeviceCountChange()
	{
		
	}
}
