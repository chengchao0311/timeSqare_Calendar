package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

//用于给事件选择时间
public class ChooseDate extends Activity {
	private static final String[] week = { "週一", "週二", "週三", "週四", "週五", "週六",
			"週日" };

	private Button fromTime;
	private Button fromDate;
	private Calendar fromDateCal;
	private Calendar toDateCal;
	private Button toDate;
	private Button toTime;
	private long dtstart;
	private long dtend;
	private Button saveBtn;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_date);
		// 初始化
		fromDateCal = Calendar.getInstance();
		toDateCal = Calendar.getInstance();
		fromDateCal.set(Calendar.SECOND, 0);
		toDateCal.set(Calendar.SECOND, 0);

		fromDate = (Button) findViewById(R.id.from_date);
		fromDate.setInputType(InputType.TYPE_NULL); // 取消弹出软键盘
		fromTime = (Button) findViewById(R.id.from_time);
		fromTime.setInputType(InputType.TYPE_NULL);

		toDate = (Button) findViewById(R.id.to_date);
		toDate.setInputType(InputType.TYPE_NULL);
		toTime = (Button) findViewById(R.id.to_time);
		toTime.setInputType(InputType.TYPE_NULL);
		saveBtn = (Button) findViewById(R.id.save_button);

		// 獲取incoming數據
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dtstart = extras.getLong("dstart");
			dtend = extras.getLong("dtend");
			if (dtstart != 0 && dtend != 0) {
				fromDateCal.setTimeInMillis(dtstart);
				toDateCal.setTimeInMillis(dtend);
			} else {
				dtstart = fromDateCal.getTimeInMillis();
				dtend = toDateCal.getTimeInMillis();
			}
		} else {
			dtstart = fromDateCal.getTimeInMillis();// 如果沒有dtstart,dtend,就一定要初始化
			dtend = toDateCal.getTimeInMillis();
		}

		fromDate.setText(buildDateString(fromDateCal));
		fromTime.setText(buildTimeString(fromDateCal));
		toDate.setText(buildDateString(toDateCal));
		toTime.setText(buildTimeString(toDateCal));
	}

	public void showTimePicker(final View v) {

		// 初始化 TimePickerDialog 的时间
		int buildHour = 0;
		int buildMinute = 0;
		if (v.getId() == R.id.from_time) {
			buildHour = fromDateCal.get(Calendar.HOUR_OF_DAY);
			buildMinute = fromDateCal.get(Calendar.MINUTE);
		} else {
			buildHour = toDateCal.get(Calendar.HOUR_OF_DAY);
			buildMinute = toDateCal.get(Calendar.MINUTE);
		}

		TimePickerDialog timepicker = new TimePickerDialog(ChooseDate.this,
				new OnTimeSetListener() {

					@SuppressLint("NewApi")
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay,
							int minute) {
						if (v.getId() == R.id.from_time) {// 点击的from_time
							// 直接把from时间这设置成选择的时间
							fromDateCal.set(fromDateCal.get(Calendar.YEAR),
									fromDateCal.get(Calendar.MONTH),
									fromDateCal.get(Calendar.DAY_OF_MONTH),
									hourOfDay, minute);

							// 就不需要设置toTime
							if (toDateCal.getTime().before(
									fromDateCal.getTime())) {
								// 如果fromDate 时间现在小于了
								// toDate的时间就把toDateCal time设置成fromDate的时间
								toDateCal.set(fromDateCal.get(Calendar.YEAR),//
										fromDateCal.get(Calendar.MONDAY), //
										fromDateCal.get(Calendar.DAY_OF_MONTH),//
										fromDateCal.get(Calendar.HOUR_OF_DAY),//
										fromDateCal.get(Calendar.MINUTE));
								toTime.setText(buildTimeString(toDateCal));
							}
							fromTime.setText(buildTimeString(fromDateCal));
						} else {// 点击的是to_Time
							if (fromTime.getText().equals("")) {// 如果from time
																// 沒有設置 就直接返回
								return;
							}
							toDateCal.set(toDateCal.get(Calendar.YEAR),//
									toDateCal.get(Calendar.MONTH),//
									toDateCal.get(Calendar.DAY_OF_MONTH),//
									hourOfDay, minute);

							if (toDateCal.getTime().before(
									fromDateCal.getTime())) {
								// 如果fromDate 时间现在小于了
								// toDate的时间就把toDateCal time设置成fromDate的时间
								toDateCal.set(fromDateCal.get(Calendar.YEAR),//
										fromDateCal.get(Calendar.MONDAY),//
										fromDateCal.get(Calendar.DAY_OF_MONTH),//
										fromDateCal.get(Calendar.HOUR_OF_DAY),//
										fromDateCal.get(Calendar.MINUTE));
							}
							toTime.setText(buildTimeString(toDateCal));
						}
						

					}

				
				}, buildHour, buildMinute, true);
		timepicker.show();
	}

	public void showDatePicker(final View v) {

		// 初始化DatePickerDialog的显示时间
		int buildYear = 0;
		int buildMonth = 0;
		int buildDate = 0;
		if (v.getId() == R.id.from_date) {
			buildYear = fromDateCal.get(Calendar.YEAR);
			buildMonth = fromDateCal.get(Calendar.MONTH);
			buildDate = fromDateCal.get(Calendar.DAY_OF_MONTH);
		} else {

			buildYear = toDateCal.get(Calendar.YEAR);
			buildMonth = toDateCal.get(Calendar.MONTH);
			buildDate = toDateCal.get(Calendar.DAY_OF_MONTH);
		}

		DatePickerDialog datePicker = new DatePickerDialog(ChooseDate.this,
				new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {

						if (v.getId() == R.id.from_date) {// 点击的是from_Date
							fromDateCal.set(year, monthOfYear, dayOfMonth);
							fromDate.setText(buildDateString(fromDateCal));
							if (toDateCal.getTime().before(
									fromDateCal.getTime())) {// 如果此時toDateCal時間小於fromDate時間
								toDateCal.set(fromDateCal.get(Calendar.YEAR),
										fromDateCal.get(Calendar.MONTH),
										fromDateCal.get(Calendar.DAY_OF_MONTH),
										fromDateCal.get(Calendar.HOUR_OF_DAY),
										fromDateCal.get(Calendar.MINUTE));
								toDate.setText(buildDateString(toDateCal));
								toTime.setText(buildTimeString(toDateCal));
							}
						} else {// 点击的是to_Date
							toDateCal.set(year, monthOfYear, dayOfMonth);
							if (toDateCal.getTime().equals(
									fromDateCal.getTime())
									|| toDateCal.getTime().after(
											fromDateCal.getTime())) {
								// 如果toDate 时间 大于或者等于fromDate的时间 则显示刚刚选择的时间
								toDateCal.set(year, monthOfYear, dayOfMonth);
							} else {
								// 如果fromDate 时间现在小于了
								// toDate的时间就把toDateCal时间的日月年设置成fromDateCal,time设置成0点
								toDateCal.set(fromDateCal.get(Calendar.YEAR),
										fromDateCal.get(Calendar.MONTH),
										fromDateCal.get(Calendar.DAY_OF_MONTH),
										fromDateCal.get(Calendar.HOUR_OF_DAY),
										fromDateCal.get(Calendar.MINUTE));
							}
							toDate.setText(buildDateString(toDateCal));
							toTime.setText(buildTimeString(toDateCal));
						}
					}
				}, buildYear, buildMonth, buildDate);
		datePicker.show();
	}

	@SuppressLint("SimpleDateFormat")
	public String buildDateString(Calendar cal) {

		String week = new SimpleDateFormat("EEE").format(cal.getTime());
		return cal.get(Calendar.YEAR) + "年" + (cal.get(Calendar.MONTH) + 1)
				+ "月" + cal.get(Calendar.DAY_OF_MONTH) + "日 " + week;
	}

	public String buildTimeString(Calendar cal) {
		String hourString = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		String minuteString = String.valueOf(cal.get(Calendar.MINUTE));
		if (hourString.length() == 1) {
			hourString = "0" + hourString;
		}

		if (minuteString.length() == 1) {
			minuteString = "0" + minuteString;
		}
		return hourString + ":" + minuteString;

	}

	public void back(View view) {
		finish();
	}

	public void done(View v) {
		// 如果兩個時間都沒有改變 就直接返回
		Intent intent = new Intent();
		intent.putExtra("dtstart", fromDateCal.getTimeInMillis());
		intent.putExtra("dtend", toDateCal.getTimeInMillis());
		setResult(CalendarPage.EVENTDETAIL_CHOOSEDATE, intent);
		finish();
	}
	
	
}
