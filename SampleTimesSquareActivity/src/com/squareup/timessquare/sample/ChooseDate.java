package com.squareup.timessquare.sample;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class ChooseDate extends Activity {
	private Button fromTime;
	private Button fromDate;


	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.date_choose_layout);
		
		fromDate = (Button) findViewById(R.id.from_date);
		fromDate.setInputType(InputType.TYPE_NULL); // 取消弹出软键盘
		fromDate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDaitePicker(v);
			}
		});
		
		fromTime = (Button) findViewById(R.id.from_time);
		fromTime.setInputType(InputType.TYPE_NULL);
		fromTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showTimePicker(v);
			}
		});
		
		
		
	}
	
	
	
	public void showTimePicker(View v){
		TimePickerDialog timepicker = new TimePickerDialog(ChooseDate.this, new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				fromTime.setText(hourOfDay + ":" + minute);
			}
		}, 12, 0, true);
		timepicker.show();
	}
	
	public void showDaitePicker(View v){
		DatePickerDialog datePicker = new DatePickerDialog(ChooseDate.this, new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				fromDate.setText(year + "." + (monthOfYear+1) + "." + dayOfMonth);
			}
		}, 2013, 2, 12);
		datePicker.show();
	}
	
	
	public void back(View view) {
		finish();
	}
}






































































































