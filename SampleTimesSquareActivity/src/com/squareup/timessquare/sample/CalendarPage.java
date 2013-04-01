package com.squareup.timessquare.sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.pnwedding.domain.PNCalendar;
import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.MonthCellDescriptor;

@SuppressLint("NewApi")
public class CalendarPage extends FragmentActivity implements
		OnPageChangeListener, ToDoListCallBack, OnItemClickListener {
	// 請求碼和回響碼
	public static boolean refreshCPVTag;
	public static int CALENDARPAGE_EVENTDETAIL_NORMAL = 11101;
	public static int CALENDARPAGE_EVENTDETAIL_EDIT = 11102;
	public static int CALENDARPAGE_EVENTDETAIL_DELETED = 11103;
	public static int EVENTDETAIL_CHOOSEDATE = 1111;
	public static int EVENTDETAIL_CHOOSEREMINDER = 1112;

	public boolean isLaunching;
	private int position;
	private CalendarPickerView calendarPickView;
	private TextView title;
	private int cellSize;
	private LinearLayout headerRow;
	private float rowFive;
	private float rowSix;
	private float rowFour;
	private ListView listView;
	private ArrayList<PNEvent> events;
	private ArrayList<PNEvent> oneDayEvents;
	private ToDoAdapter toDoAdapter;
	private PNCalendar pnCalendar;
	private Handler handler;
	private int monthIndex;

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_picker);
		handler = new Handler();
		isLaunching = true;
		// 初始化配置文件
		SharedPreferences appConfig = getSharedPreferences("app_config",
				MODE_PRIVATE);
		Map<String, ?> all = appConfig.getAll();
		checkConfig(appConfig, "calendar_max_year", 3);
		checkConfig(appConfig, "calendar_start_year", 1995);
		checkConfig(appConfig, "calendar_start_month", 0);
		checkConfig(appConfig, "calendar_start_date", 1);

		// 計算每天格子的大小 為調整buttom做準備
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		cellSize = displayMetrics.widthPixels / 7;

		View addEventLayout = getLayoutInflater().inflate(
				R.layout.add_event_row, null);
		// ListView 部分
		listView = (ListView) findViewById(R.id.listView);
		oneDayEvents = new ArrayList<PNEvent>();// 第一次先创建一个空的容器
		listView.addHeaderView(addEventLayout);
		toDoAdapter = new ToDoAdapter(this, R.layout.to_do_list_item,
				R.id.to_do_item, oneDayEvents);
		listView.setOnItemClickListener(this);
		listView.setAdapter(toDoAdapter);

		// 初始化CalendarPickView的時間跨度
		final Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.YEAR, appConfig.getInt("calendar_max_year", 3));
		calendarPickView = (CalendarPickerView) findViewById(R.id.calendar_view);
		calendarPickView.toDoListCallBack = this;// 讲CallBack 设为自身
		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.YEAR,
				appConfig.getInt("calendar_start_year", 1995));//
		startTime.set(Calendar.MONTH,
				appConfig.getInt("calendar_start_month", 0));//
		startTime.set(Calendar.DAY_OF_MONTH,
				appConfig.getInt("calendar_start_date", 1));//
		Date startDate = startTime.getTime();

		try {
			pnCalendar = PNCalendar.findPNCalendarByDisplayName(
					CalendarPage.this, "kevin@bhiner.com");// 需要創建的Calendar
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (NoSuchFieldException e) {
		}
		events = new ArrayList<PNEvent>();

		// 开始初始化Calendar
		calendarPickView.init(new Date(), startDate, targetDate.getTime(),
				getSupportFragmentManager(), events);

		calendarPickView.setVerticalScrollBarEnabled(false);
		calendarPickView.setEnabled(false);
		calendarPickView.setOnPageChangeListener(this);
		monthIndex = calendarPickView.selectedCell.getMonthIndex();//把选中月份设置为今天
		// 初始化Calendar位置
		calendarPickView.setLayoutParams(new LinearLayout.LayoutParams(
				calendarPickView.getLayoutParams().width, (cellSize * 6)));
		position = calendarPickView.selectedIndex;

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

		// handle action
		addEventLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent();
				intent.setClass(CalendarPage.this, EventDetail.class);
				intent.putExtra("pnCalendar", pnCalendar);
				intent.putExtra("request_code",
						CalendarPage.CALENDARPAGE_EVENTDETAIL_NORMAL);
				intent.putExtra("selected_date", calendarPickView
						.getSelectedDate().getTime());
				CalendarPage.this.startActivityForResult(intent,
						CALENDARPAGE_EVENTDETAIL_EDIT);
			}
		});
		findViewById(R.id.next).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position < calendarPickView.months.size() - 1) {
					calendarPickView.setCurrentItem(position++);
				}
			}
		});
		findViewById(R.id.prev).setOnClickListener(new OnClickListener() {

			@SuppressLint("NewApi")
			@Override
			public void onClick(View arg0) {
				if (position > 0) {
					calendarPickView.setCurrentItem((position--));
				}
			}
		});

	}

	// ***************************************//
	// ******************生命週期方法************//
	// ***************************************//
	@Override
	protected void onDestroy() {
		super.onDestroy();
		calendarPickView.toDoListCallBack = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.FragmentActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		refresCPVAndToDoList();
	}

	/**
	 * 
	 */
	public void refresCPVAndToDoList() {
		pnCalendar.queryCalendaerEventsRefreshCPV(this, events,
				calendarPickView);
		pnCalendar.queryAndFillOneDayEventAndRefreshToDoList(this,
				calendarPickView.selectedCell.getDate().getTime(),
				oneDayEvents, toDoAdapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == CalendarPage.CALENDARPAGE_EVENTDETAIL_NORMAL) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				long time = extras.getLong("dtstart");
				// 獲得創建好的事件的時間的midNight
				final MonthCellDescriptor cell = calendarPickView
						.getCellByDate(new Date(time));
				if (cell != null) {
					if (cell.isCurrentMonth()) {
						calendarPickView.setSelectedCell(cell);
						calendarPickView.scrollToSelectedMonth(cell
								.getMonthIndex());
						monthIndex = cell.getMonthIndex();
					}
				}
			}
		}
	}

	// ***************************************//
	// ******************接口實現方法***********//
	// ***************************************//
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
			if (arg0 != monthIndex) {//如果当前展示的月份 不是选中日期所在的月份就清空toDoList
				oneDayEvents.clear();
				toDoAdapter.notifyDataSetChanged();
			} else {
				pnCalendar.queryAndFillOneDayEventAndRefreshToDoList(this,//
						calendarPickView.selectedCell.getDate().getTime(),//
						oneDayEvents, toDoAdapter);
			}

		title.setText(calendarPickView.months.get(arg0).getLabel());
		// 计算在不同行數的情况下 bottom的位置
		if (rowFive == 0 || rowSix == 0 || rowFour == 0) {
			rowFive = calendarPickView.getY() + (cellSize * 5);
			rowSix = calendarPickView.getY() + (cellSize * 6);
			rowFour = calendarPickView.getY() + (cellSize * 4);
		}
		// 调整位置
		if (calendarPickView.cells.get(arg0).size() == 5) {
			if (listView.getY() != (rowFive)) {
				listView.setY(rowFive);
			}
		} else if (calendarPickView.cells.get(arg0).size() == 4) {
			if (listView.getY() != rowFour) {
				listView.setY(rowFour);
			}
		} else {
			if (listView.getY() != rowSix) {
				listView.setY(rowSix);
			}
		}
	}

	@Override
	public void clearAndrefresh(int monthIndex) {// 当点中的日期 没有事件时使用
		this.monthIndex = monthIndex;
		oneDayEvents.clear();
		toDoAdapter.notifyDataSetChanged();
	}

	@Override
	public void showEventsForTheDay(int monthIndex) {// 查詢數據庫 更新todo list
		this.monthIndex = monthIndex;
		pnCalendar.queryAndFillOneDayEventAndRefreshToDoList(this,
				calendarPickView.selectedCell.getDate().getTime(),
				oneDayEvents, toDoAdapter);
	}

	// ***************************************//
	// ******************工具方法***************//
	// ***************************************//
	public void checkConfig(SharedPreferences appConfig, String key, int val) {
		Map<String, ?> map = appConfig.getAll();
		if (!map.containsKey(key)) {
			appConfig.edit().putInt(key, val).commit();
		}
	}

	public void checkConfig(SharedPreferences appConfig, String key, long val) {
		Map<String, ?> map = appConfig.getAll();
		if (!map.containsKey(key)) {
			appConfig.edit().putLong(key, val).commit();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		PNEvent pnEvent = oneDayEvents.get(position - 1);
		Intent intent = new Intent();
		intent.putExtra("pnEvent", pnEvent);
		intent.putExtra("pnCalendar", pnCalendar);
		intent.putExtra("request_code", CALENDARPAGE_EVENTDETAIL_EDIT);
		intent.setClass(this, EventDetail.class);
		startActivityForResult(intent, CALENDARPAGE_EVENTDETAIL_EDIT);
	}

}
