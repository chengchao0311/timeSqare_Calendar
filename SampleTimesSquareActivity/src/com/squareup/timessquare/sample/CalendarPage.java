package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.CalendarPickerView;

public class CalendarPage extends FragmentActivity implements
		OnPageChangeListener, ToDoListCallBack {
	// 請求碼和回響碼
	public static int CALENDARPAGE_EVENTDETAIL_ADD_EMPTY = 1110;
	public static int EVENTDETAIL_CHOOSEDATE = 1111;
	public static int EVENTDETAIL_CHOOSEREMINDER = 1112;
	public SharedPreferences appConfig;// 配置文件

	private int position;
	private CalendarPickerView calendar;
	private TextView title;
	private View addEventBtn;
	private int cellSize;
	private LinearLayout headerRow;
	private RelativeLayout bottom;
	private float rowFive;
	private float rowSix;
	private float rowFour;
	private ListView listView;
	private ArrayList<PNEvent> events;
	private ArrayList<PNEvent> oneDayEvents;
	private ToDoAdapter toDoAdapter;
	private PNCalendar pnCalendar;
	private Handler handler;

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_picker);
		handler = new Handler();
		// 初始化配置文件
		appConfig = getSharedPreferences("app_config", MODE_PRIVATE);
		Map<String, ?> all = appConfig.getAll();
		checkConfigContainsInt(all, "calendar_max_year", 3);
		checkConfigContainsInt(all, "calendar_start_year", 1995);
		checkConfigContainsInt(all, "calendar_start_month", 0);
		checkConfigContainsInt(all, "calendar_start_date", 1);
		
		// 計算每天格子的大小 為調整buttom做準備
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		cellSize = displayMetrics.widthPixels / 7;
		bottom = (RelativeLayout) findViewById(R.id.bottom);

		// 初始化CalendarPickView的時間跨度
		final Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.YEAR, appConfig.getInt("calendar_max_year", 3));
		calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
		calendar.toDoListCallBack = this;// 讲CallBack 设为自身
		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.YEAR,
				appConfig.getInt("calendar_start_year", 1995));//
		startTime.set(Calendar.MONTH,
				appConfig.getInt("calendar_start_month", 0));//
		startTime.set(Calendar.DAY_OF_MONTH,
				appConfig.getInt("calendar_start_date", 1));//
		Date startDate = startTime.getTime();
		
		try {
			pnCalendar = PNCalendar.findPNCalendarByDisplayName(CalendarPage.this,
					"kevin@bhiner.com");// 需要創建的Calendar
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		}
		events =  new ArrayList<PNEvent>();
		
		// 开始初始化Calendar
		calendar.init(new Date(), startDate, targetDate.getTime(),
				getSupportFragmentManager(), events);
		pnCalendar.queryEventsFromCalendar(CalendarPage.this, events, calendar, handler);
		calendar.setVerticalScrollBarEnabled(false);
		calendar.setEnabled(false);
		calendar.setOnPageChangeListener(this);

		// 初始化Calendar位置
		calendar.setLayoutParams(new LinearLayout.LayoutParams(calendar
				.getLayoutParams().width, (cellSize * 6)));
		position = calendar.selectedIndex;

		// title部分
		title = (TextView) findViewById(R.id.title);
		title.setText(new SimpleDateFormat("MMMM yyyy").format(targetDate
				.getTime()));
		
		// 星期部分
		headerRow = (LinearLayout) findViewById(R.id.weekRow);
		for (int c = Calendar.SUNDAY; c <= Calendar.SATURDAY; c++) {
			targetDate.set(Calendar.DAY_OF_WEEK, c);
			final TextView textView = (TextView) headerRow.getChildAt(c - 1);
			textView.setText(new SimpleDateFormat("EEE").format(targetDate
					.getTime()));
		}

		// ListView 部分
		listView = (ListView) findViewById(R.id.listView);
		oneDayEvents = new ArrayList<PNEvent>();// 第一次先创建一个空的容器
		toDoAdapter = new ToDoAdapter(this, R.layout.to_do_list_item,
				R.id.to_do_item, oneDayEvents);
		listView.setAdapter(toDoAdapter);

		// handle action
		addEventBtn = findViewById(R.id.addEvent);
		addEventBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				int size = calendar.months.size();
				Intent intent = new Intent();
				intent.setClass(CalendarPage.this, EventDetail.class);
				intent.putExtra("pnCalendar", pnCalendar);
				intent.putExtra("request_code",
						CALENDARPAGE_EVENTDETAIL_ADD_EMPTY);
				intent.putExtra("selected_date", calendar.getSelectedDate()
						.getTime());
				CalendarPage.this.startActivityForResult(intent, appConfig
						.getInt("calendarpage_eventdetail",
								CALENDARPAGE_EVENTDETAIL_ADD_EMPTY));
			}
		});
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position < calendar.months.size() - 1) {
					calendar.setCurrentItem(position++);
				}
			}
		});
		findViewById(R.id.prev).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position > 0) {
					calendar.setCurrentItem((position--));
				}
			}
		});

	}
	//***************************************//
	//******************生命週期方法************//
	//***************************************//
	@Override
	protected void onDestroy() {
		super.onDestroy();
		calendar.toDoListCallBack = null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		pnCalendar.queryEventsFromCalendar(CalendarPage.this, events, calendar, handler);
		calendar.getAdapter().notifyDataSetChanged();
	}
	
	//***************************************//
	//******************接口實現方法***********//
	//***************************************//
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
		
		oneDayEvents.clear();//清空listview
		toDoAdapter.notifyDataSetChanged();
		
		title.setText(calendar.months.get(arg0).getLabel());
		// 计算在不同行數的情况下 bottom的位置
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
	public void showEventsForTheDay(ArrayList<PNEvent> oneDayEventsFromCell) {
		oneDayEvents.clear();
		if (oneDayEventsFromCell != null && oneDayEventsFromCell.size() > 0) {
			for (int i = 0; i < oneDayEventsFromCell.size(); i++) {
				oneDayEvents.add(oneDayEventsFromCell.get(i));
			}
		}
		toDoAdapter.notifyDataSetChanged();
	}

	
	//***************************************//
	//******************工具方法***************//
	//***************************************//
	public void checkConfigContainsInt(Map<String, ?> map, String key, int val) {
		if (!map.containsKey(key)) {
			appConfig.edit().putInt(key, val).commit();
		}
	}

}
