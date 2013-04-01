package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.PNEvent;
import com.pnwedding.domain.ReminderTimeDescriptor;
import com.pnwedding.utils.Utils;
import com.squareup.timessquare.CalendarPickerView;

public class EventDetail extends Activity implements OnClickListener {
	private static final long NO_EVENT_TAG = 1115001;
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
	private Button saveBtn;
	private int requestCode;
	private SimpleDateFormat timeFormater;
	private Handler handler;
	private long reminderTimeMills;
	private Button deleteBtn;
	private RelativeLayout choosDateArea;
	private RelativeLayout remindeArea;
	private long eventId = EventDetail.NO_EVENT_TAG;
	private ScrollView scrollView;

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_detail);

		// 初始化
		simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
		timeFormater = new SimpleDateFormat("HH" + "點" + "mm" + "分");
		fromCal = Calendar.getInstance();
		toCal = Calendar.getInstance();
		reminderTimeMills = 0L;
		// 初始化View
		saveBtn = (Button) findViewById(R.id.save_button);
		titleView = (EditText) findViewById(R.id.title_edit);
		desView = (EditText) findViewById(R.id.description_edit_text);
		choosDateTextView = (TextView) findViewById(R.id.date_edit);
		reminderTextView = (TextView) findViewById(R.id.reminder_text);
		deleteBtn = (Button) findViewById(R.id.deleteBtn);
		choosDateArea = (RelativeLayout) findViewById(R.id.choose_date_area);
		remindeArea = (RelativeLayout) findViewById(R.id.reminder);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
	

		//点击事件处理
		choosDateArea.setOnClickListener(new OnClickListener() {

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
		
		remindeArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(EventDetail.this, ChooseReminder.class);
				intent.putExtra("reminder_timeMill", reminderTimeMills);
				EventDetail.this.startActivityForResult(intent,
						CalendarPage.EVENTDETAIL_CHOOSEREMINDER);
			}
		});
		
		desView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					scrollView.post(new Runnable() {
						public void run() {
							scrollView.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});
				}
			}
		});
		
		// 獲得傳來的參數，决定当前状态,顯示日期
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			requestCode = extras.getInt("request_code");
			pnCalendar = extras.getParcelable("pnCalendar");
			if (requestCode == CalendarPage.CALENDARPAGE_EVENTDETAIL_NORMAL) {// 點擊的是增加新按鈕的btn,初始化日期
				deleteBtn.setVisibility(View.GONE);
				long timemills = extras.getLong("selected_date");
				String selectedDate = "";
				if (timemills != 0) {
					fromCal.setTimeInMillis(timemills);
					toCal.setTimeInMillis(timemills);
					selectedDate = simpleDateFormat.format(fromCal.getTime());
				}
				choosDateTextView.setText(selectedDate);
				saveBtn.setOnClickListener(this);
			} else if (requestCode == CalendarPage.CALENDARPAGE_EVENTDETAIL_EDIT) {
				// 點擊的是to/ do
				// 獲取Event 對頁面進行刷新 // list的某个item,编辑事件
				PNEvent pnEvent = extras.getParcelable("pnEvent");
				eventId = pnEvent._id;
				titleView.setText(pnEvent.title);
				desView.setText(pnEvent.description);
				updateDateText(pnEvent.dtstart, pnEvent.dtend);
				titleView.setEnabled(false);
				desView.setEnabled(false);
				// 查詢數據庫找到事件對應的reminder,和配置文件比對,得到key
				if (pnEvent.hasAlarm) {
					reminderTimeMills = pnCalendar.queryReminder(this,
							pnEvent._id);
					if (reminderTimeMills != 0) {
						Set<String> stringPropertyNames = Utils
								.getReminderProperties().stringPropertyNames();
						for (String key : stringPropertyNames) {
							String valStr = Utils.getReminderProperties()//
									.getProperty(key);
							long val = Long.parseLong(valStr);
							if (val == reminderTimeMills) {
								reminderTextView.setText(key);
								break;
							}
						}
					}
				} else {
					reminderTextView.setText("無");
				}
				// 改變btn 狀態
				choosDateArea.setEnabled(false);
				remindeArea.setEnabled(false);
				updateSaveBtnSatuts();
				
				//编辑按钮点击后的事件
				saveBtn.setText(getResources().getString(R.string.edit));
				saveBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						saveBtn.setText(getResources().getString(R.string.done));
						
						titleView.setEnabled(true);
						desView.setEnabled(true);
						titleView.requestFocus();
						
						// update Event;
						deleteBtn.setVisibility(ViewGroup.VISIBLE);
						deleteBtn.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								AlertDialog.Builder builder = new AlertDialog.Builder(EventDetail.this);
								builder.setTitle("确定删除事件?");
								builder.setPositiveButton(R.string.delete_event, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										setResult(CalendarPage.CALENDARPAGE_EVENTDETAIL_DELETED);
										finish();
									}
								});
								builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
								builder.show();
							}
						});
						choosDateArea.setEnabled(true);
						remindeArea.setEnabled(true);
					}
				});
			}
		}

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
			updateDateText(dtstart, dtstart);
		}

		if (resultCode == CalendarPage.EVENTDETAIL_CHOOSEREMINDER) {
			Bundle extras = data.getExtras();
			reminderTimeDescriptor = extras
					.getParcelable("reminderTimeDescriptor");
			reminderTextView.setText(reminderTimeDescriptor.text);
			reminderTimeMills = reminderTimeDescriptor.timeMills;
		}
	}

	/**
	 * 
	 */
	public void updateDateText(long dtstart, long dtend) {
		fromCal.setTimeInMillis(dtstart);
		toCal.setTimeInMillis(dtend);
		if (CalendarPickerView.sameDate(fromCal, toCal)) {
			if (fromCal.getTime().equals(toCal.getTime())) {
				choosDateTextView.setText(" "
						+ simpleDateFormat.format(fromCal.getTime()) + //
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
	@Override
	public void onClick(View v) {
		insertEventAndBack();
	}

	// ***************************************//
	// ******************按鈕事件方法***********//
	// ***************************************//
	public void back(View view) {
		finish();
	}

	// *********************************************//
	// *********************抽出方法******************//
	// *********************************************//
	public void insertEventAndBack() {
		// 獲取提醒時間 如果沒有就是0
		long remiderTimeMill = 0L;
		if (reminderTimeDescriptor != null) {
			remiderTimeMill = reminderTimeDescriptor.timeMills;
		}
		// 插入數據
		pnCalendar.insertEvent(EventDetail.this,
				titleView.getText().toString(), //
				desView.getText().toString(), fromCal, toCal, remiderTimeMill);
		// 將更改或增加的事件的時間傳回去
		Intent intent = new Intent();
		CalendarPickerView.setMidnight(fromCal);
		CalendarPickerView.setMidnight(toCal);
		intent.putExtra("dtstart", fromCal.getTimeInMillis());
		intent.putExtra("dtend", toCal.getTimeInMillis());
		setResult(CalendarPage.CALENDARPAGE_EVENTDETAIL_NORMAL, intent);
		finish();
	}

	public void updateSaveBtnSatuts() {
		if (!titleView.getText().toString().equalsIgnoreCase("")) {
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
