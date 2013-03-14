package com.squareup.timessquare.sample;

import static android.widget.Toast.LENGTH_SHORT;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarRowView;
import com.squareup.timessquare.ToDoAdapter;
import com.squareup.timessquare.ToDoListCallBack;

public class CalendarPage extends FragmentActivity implements
		OnPageChangeListener, ToDoListCallBack {
	private static final String TAG = "SampleTimesSquareActivity";

	private int position;
	private CalendarPickerView calendar;
	private TextView title;
	private View done;
	private int cellSize;
	private LinearLayout headerRow;
	private RelativeLayout bottom;
	private float rowFive;
	private float rowSix;
	private float rowFour;
	private ListView listView;
	private ArrayList<PNEvent> events;

	
	@SuppressLint("InlinedApi")
	public static final String[] EVENT_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME, // 2
			Calendars.OWNER_ACCOUNT // 3
	};

	private ToDoAdapter toDoAdapter;

	private ArrayList<PNEvent> oneDayEvents;
	
	
	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_picker);

		// calculate the cellsize for calendar cell
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		cellSize = displayMetrics.widthPixels / 7;
		bottom = (RelativeLayout) findViewById(R.id.bottom);

		// init the calendar
		final Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.YEAR, 3);
		calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
		calendar.toDoListCallBack = this;//讲CallBack 设为自身
		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.YEAR, 1995);
		startTime.set(Calendar.MONTH, 0);
		startTime.set(Calendar.DAY_OF_MONTH, 1);

		Date startDate = startTime.getTime();
		PNCalendar pnCalendar = null;
		try {
			pnCalendar = PNCalendar.findPNCalendarByDisplayName(this, "kevin@bhiner.com");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		
		//增加日历事件
			//ListView 部分
		listView = (ListView) findViewById(R.id.listView);
		events = pnCalendar.queryEventsFromCalendar(this);
		oneDayEvents = new ArrayList<PNEvent>();//第一次先创建一个空的容器
		
		toDoAdapter = new ToDoAdapter(this, R.layout.to_do_list, R.id.to_do_item, oneDayEvents);
		listView.setAdapter(toDoAdapter);
	
		//开始初始化Calendar
		calendar.init(new Date(), startDate, targetDate.getTime(),
				getSupportFragmentManager(),events);
		calendar.setVerticalScrollBarEnabled(false);
		calendar.setEnabled(false);
		calendar.setOnPageChangeListener(this);
		
		
		// 星期部分
		headerRow = (LinearLayout) findViewById(R.id.weekRow);
		for (int c = Calendar.SUNDAY; c <= Calendar.SATURDAY; c++) {
			targetDate.set(Calendar.DAY_OF_WEEK, c);
			final TextView textView = (TextView) headerRow.getChildAt(c - 1);
			textView.setText(new SimpleDateFormat("EEE").format(targetDate
					.getTime()));
		}
		//title 部分
		title = (TextView) findViewById(R.id.title);
		title.setText(new SimpleDateFormat("MMMM yyyy").format(targetDate
				.getTime()));
		calendar.setLayoutParams(new LinearLayout.LayoutParams(calendar
				.getLayoutParams().width, (cellSize * 6)));
		position = calendar.selectedIndex;
		
		
		// handle action
		done =  findViewById(R.id.addEvent);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(CalendarPage.this, EventDetail.class);
				intent.putExtra("selected_date", calendar.getSelectedDate().getTime());
				CalendarPage.this.startActivityForResult(intent, 0);
			}
		});
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position < calendar.months.size() - 1) {
					calendar.setCurrentItem(position += 1);
				}
			}
		});
		findViewById(R.id.prev).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position > 0) {
					calendar.setCurrentItem((position -= 1));
				}
			}
		});

	}

	// 改变bottom位置
	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@SuppressLint("NewApi")
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@SuppressLint("NewApi")
	@Override
	public void onPageSelected(int arg0) {
		title.setText(calendar.months.get(arg0).getLabel());
		// 计算在不同的情况下 bottom的位置
		if (rowFive == 0 || rowSix == 0 || rowFour == 0) {
			rowFive = calendar.getY() + (cellSize * 5);
			rowSix = calendar.getY() + (cellSize * 6);
			rowFour = calendar.getY() + (cellSize * 4);
		}
		// 调整位置
		if (calendar.cells.get(arg0).size() == 5) {
			if (bottom.getY() != (rowFive)) {
				bottom.setY(rowFive);
			}
		} else if (calendar.cells.get(arg0).size() == 4) {
			if (bottom.getY() != rowFour) {
				bottom.setY(rowFour);
			}
		} else {
			if (bottom.getY() != rowSix) {
				bottom.setY(rowSix);
			}
		}
	}

	@Override
	public void showEventsOfTheDay(ArrayList<PNEvent> oneDayEventsFromCell) {
		oneDayEvents.clear();
		if (oneDayEventsFromCell != null && oneDayEventsFromCell.size() > 0) {
			for (int i = 0; i < oneDayEventsFromCell.size(); i++) {
				oneDayEvents.add(oneDayEventsFromCell.get(i));
			}
		}
		toDoAdapter.notifyDataSetChanged();
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		calendar.toDoListCallBack = null;
	}
	






}
