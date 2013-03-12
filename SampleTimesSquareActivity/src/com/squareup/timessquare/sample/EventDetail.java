package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EventDetail extends Activity {
	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);
		Bundle extras = getIntent().getExtras();
		long timemills = extras.getLong("selected_date");
		String selectedDate = ""; 
		if (timemills != 0) {
			selectedDate = new SimpleDateFormat("yyyy.MM.dd")
					.format(timemills);
		}
		TextView date_edit = (TextView) findViewById(R.id.date_edit);
		date_edit.setText(selectedDate);
		
		findViewById(R.id.choose_date_area).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(EventDetail.this, ChooseDate.class);
				EventDetail.this.startActivity(intent);
			}
		});
		
	}
	
	public void back(View view) {
		finish();
	}
}
