package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.pnwedding.domain.PNEvent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class EventDetail extends Activity {
	@SuppressLint("SimpleDateFormat")
	public static PNEvent event;
	private SimpleDateFormat simpleDateFormat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);
		//初始化
		simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		event = new PNEvent();
		
		//獲得傳來的參數，顯示日期
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			long timemills = extras.getLong("selected_date");
			String selectedDate = ""; 
			
			if (timemills != 0) {
				selectedDate = simpleDateFormat.format(timemills);
			}
			TextView date_edit = (TextView) findViewById(R.id.date_edit);
			date_edit.setText(selectedDate);
		}
		
		//選擇時期被點擊
		findViewById(R.id.choose_date_area).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(EventDetail.this, ChooseDate.class);
				EventDetail.this.startActivityForResult(intent,1112);
			}
		});
	}
	
	public void back(View view) {
		finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1113) {
			TextView v = (TextView)findViewById(R.id.choose_date_area).findViewById(R.id.date_edit);
			Calendar fromCal = Calendar.getInstance();
			Calendar toCal = Calendar.getInstance();
			fromCal.setTimeInMillis(event.dtstart);
			toCal.setTimeInMillis(event.dtend);
			v.setText("  "+ simpleDateFormat.format(fromCal.getTime()) + " 到 " + simpleDateFormat.format(toCal.getTime()));
		}
	}
	
}
