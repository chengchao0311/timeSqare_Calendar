package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.PNEvent;
import com.pnwedding.domain.ReminderTimeDescriptor;

public class EventDetail extends Activity {
	@SuppressLint("SimpleDateFormat")
	public PNEvent event;
	public CalendarPage calendarPage;
	private SimpleDateFormat simpleDateFormat;
	private Calendar fromCal;
	private Calendar toCal;
	private TextView titleView;
	private EditText desView;
	private PNCalendar pnCalendar;
	private TextView choosDateTextView;
	private ReminderTimeDescriptor reminderTimeDescriptor;
	private boolean editReminderFlag;
	private boolean editDateFlag;
	private TextView reminderTextView;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);
		// 初始化
		simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		event = new PNEvent();
		fromCal = Calendar.getInstance();
		toCal = Calendar.getInstance();

		titleView = (TextView) findViewById(R.id.title_text);
		desView = (EditText) findViewById(R.id.description_edit_text);
		choosDateTextView = (TextView)findViewById(R.id.date_edit);
		reminderTextView = (TextView) findViewById(R.id.reminder_text);
		// 獲得傳來的參數，顯示日期
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			long requestCode = extras.getLong("request_code");
			pnCalendar = extras.getParcelable("pnCalendar");
			if (requestCode == CalendarPage.CALENDARPAGE_EVENTDETAIL_ADD_EMPTY) {
				// 點擊的是增加新按鈕的btn,初始化日期
				long timemills = extras.getLong("selected_date");
				String selectedDate = "";
				if (timemills != 0) {
					selectedDate = simpleDateFormat.format(timemills);
					fromCal.setTimeInMillis(timemills);
					toCal.setTimeInMillis(timemills);
				}
				choosDateTextView.setText(selectedDate);
			}
		}

		// 選擇時期被點擊
		findViewById(R.id.choose_date_area).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent();
						intent.setClass(EventDetail.this, ChooseDate.class);
						intent.putExtra("dstart", fromCal.getTimeInMillis());
						intent.putExtra("dtend", toCal.getTimeInMillis());
						EventDetail.this.startActivityForResult(intent,
								CalendarPage.EVENTDETAIL_CHOOSEDATE);
					}
				});

		// 選擇提醒被點擊
		findViewById(R.id.reminder).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(EventDetail.this, ChooseReminder.class);
				EventDetail.this.startActivityForResult(intent, 1114);
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
		if (resultCode == CalendarPage.EVENTDETAIL_CHOOSEDATE) {
			Bundle extras = data.getExtras();
			long dtstart = extras.getLong("dstart");
			long dtend = extras.getLong("dtend");

			fromCal.setTimeInMillis(dtstart);
			toCal.setTimeInMillis(dtend);
			choosDateTextView.setText(" "
					+ simpleDateFormat.format(fromCal.getTime()) + " "
					+ getResources().getString(R.string.to) + " "
					+ simpleDateFormat.format(toCal.getTime()));
		}

		if (resultCode == CalendarPage.EVENTDETAIL_CHOOSEREMINDER) {
			Bundle extras = data.getExtras();
			reminderTimeDescriptor = extras
					.getParcelable("reminderTimeDescriptor");
			reminderTextView.setText(reminderTimeDescriptor.text);
		}
	}

	public void done(View view) {
		String inputTitle = titleView.getText().toString();
		String des = desView.getText().toString();

		if (inputTitle.equalsIgnoreCase("")) {
			inputTitle = getResources().getString(R.string.new_event);// 如果沒有標題
		}
		if (!des.equalsIgnoreCase("")) {
			event.description = des;
		}
		event.title = inputTitle;
		 
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
