package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.ReminderTimeDescriptor;
import com.squareup.timessquare.CalendarPickerView;

public class EventDetail extends Activity {

	@SuppressLint("SimpleDateFormat")
	public CalendarPage calendarPage;
	private SimpleDateFormat simpleDateFormat;
	private Calendar fromCal;
	private Calendar toCal;
	private EditText titleView;
	private EditText desView;
	private PNCalendar pnCalendar;
	private TextView choosDateTextView;
	private ReminderTimeDescriptor reminderTimeDescriptor;
	private TextView reminderTextView;

	private boolean editReminderFlag;
	private boolean editDateFlag;
	private boolean editTitleFlag;
	private boolean editDesFlag;
	private boolean editAttFlag;

	private String oldTitle;
	private String oldDes;
	private Button saveBtn;
	private int requestCode;
	private SimpleDateFormat timeFormater;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);

		// 初始化
		simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		timeFormater = new SimpleDateFormat("HH" + "點" + "mm" + "分");
		fromCal = Calendar.getInstance();
		toCal = Calendar.getInstance();

		// 初始化View
		saveBtn = (Button) findViewById(R.id.save_button);
		titleView = (EditText) findViewById(R.id.title_edit);
		desView = (EditText) findViewById(R.id.description_edit_text);
		choosDateTextView = (TextView) findViewById(R.id.date_edit);
		reminderTextView = (TextView) findViewById(R.id.reminder_text);

		oldTitle = titleView.getText().toString();
		oldDes = desView.getText().toString();
		// 獲得傳來的參數，顯示日期
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			requestCode = extras.getInt("request_code");
			pnCalendar = extras.getParcelable("pnCalendar");
			if (requestCode == CalendarPage.CALENDARPAGE_EVENTDETAIL) {
				// 點擊的是增加新按鈕的btn,初始化日期
				long timemills = extras.getLong("selected_date");
				String selectedDate = "";
				if (timemills != 0) {
					fromCal.setTimeInMillis(timemills);
					toCal.setTimeInMillis(timemills);
					selectedDate = simpleDateFormat.format(fromCal.getTime());
				}
				choosDateTextView.setText(selectedDate);
			}
		}

		// ----處理點擊事件----//
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

		// -----處理文字改變事件------//
		titleView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String content = titleView.getText().toString();
				if (!content.equalsIgnoreCase(oldTitle) && !content.equals("")) {// 如果文字被改變
					editTitleFlag = true;
				} else {
					editTitleFlag = false;
				}
				updateSaveBtnSatuts();
			}
		});

		desView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!desView.getText().toString().equalsIgnoreCase(oldDes)) {
					editDesFlag = true;
				} else {
					editDesFlag = false;
				}
				updateSaveBtnSatuts();
			}
		});
	}

	// **************************************//
	// *****************生命週期方法***********//
	// **************************************//

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == CalendarPage.EVENTDETAIL_CHOOSEDATE) {
			Bundle extras = data.getExtras();
			long dtstart = extras.getLong("dtstart");
			long dtend = extras.getLong("dtend");
			fromCal.setTimeInMillis(dtstart);
			toCal.setTimeInMillis(dtend);

			if (CalendarPickerView.sameDate(fromCal, toCal)) {
				if (fromCal.getTime().equals(toCal.getTime())) {
					choosDateTextView.setText(" " + simpleDateFormat.format(fromCal.getTime()) + //
							" " + timeFormater.format(fromCal.getTime()));
				} else {
					choosDateTextView.setText(" "
							+ simpleDateFormat.format(fromCal.getTime())//
							+ " " + timeFormater.format(fromCal.getTime())//
							+ " 到 " + timeFormater.format(toCal.getTime())//
					);
				}
			} else {
				choosDateTextView.setText(" "
						+ simpleDateFormat.format(fromCal.getTime()) + " "
						+ getResources().getString(R.string.to) + " "
						+ simpleDateFormat.format(toCal.getTime()));
			}

			editDateFlag = true;
			updateSaveBtnSatuts();
		}

		if (resultCode == CalendarPage.EVENTDETAIL_CHOOSEREMINDER) {
			Bundle extras = data.getExtras();
			reminderTimeDescriptor = extras
					.getParcelable("reminderTimeDescriptor");
			reminderTextView.setText(reminderTimeDescriptor.text);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// ***************************************//
	// ******************接口實現方法***********//
	// ***************************************//

	// ***************************************//
	// ******************按鈕事件方法***********//
	// ***************************************//
	public void back(View view) {
		finish();
	}

	public void done(View view) {
		// 插入數據
		pnCalendar.insertEvent(this, titleView.getText().toString(), desView//
				.getText().toString(), fromCal, toCal, "0");
		// 重置（可能多餘）
		editAttFlag = false;
		editDateFlag = false;
		editDesFlag = false;
		editReminderFlag = false;
		editTitleFlag = false;
		// 將更改或增加的事件的時間傳回去
		Intent intent = new Intent();
		CalendarPickerView.setMidnight(fromCal);
		CalendarPickerView.setMidnight(toCal);
		intent.putExtra("dtstart", fromCal.getTimeInMillis());
		intent.putExtra("dtend", toCal.getTimeInMillis());
		setResult(CalendarPage.CALENDARPAGE_EVENTDETAIL, intent);
		finish();
	}

	private void updateSaveBtnSatuts() {
		if (editAttFlag || editDateFlag || editDesFlag || editReminderFlag
				|| editTitleFlag) {
			saveBtn.setClickable(true);
			saveBtn.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.head_button));
		} else {
			saveBtn.setClickable(false);
			saveBtn.setBackgroundDrawable(getResources().getDrawable(
					R.drawable.head_button_unclicked));
		}
	}

}
