package com.squareup.timessquare.sample;

import static android.widget.Toast.LENGTH_SHORT;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pnwedding.domain.PNCalendar;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarRowView;

public class SampleTimesSquareActivity extends FragmentActivity implements
		OnPageChangeListener {
	private static final String TAG = "SampleTimesSquareActivity";
	@SuppressLint("InlinedApi")
	public static final String[] EVENT_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME, // 2
			Calendars.OWNER_ACCOUNT // 3
	};

	private int position;
	private CalendarPickerView calendar;
	private TextView title;
	private TextView done;
	private int cellSize;
	private CalendarRowView headerRow;
	private RelativeLayout bottom;
	private float rowFive;
	private float rowSix;
	private float rowFour;

	@SuppressLint({ "SimpleDateFormat", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar_picker);

		try {
			PNCalendar pnCalendar = queryCalendar();
			String _id = pnCalendar._id;
			System.out.println(_id);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		// calculate the cellsize for calendar cell
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		cellSize = displayMetrics.widthPixels / 7;
		bottom = (RelativeLayout) findViewById(R.id.bottom);

		// init the calendar
		final Calendar targetDate = Calendar.getInstance();
		targetDate.add(Calendar.YEAR, 3);
		calendar = (CalendarPickerView) findViewById(R.id.calendar_view);

		Calendar startTime = Calendar.getInstance();
		startTime.set(Calendar.YEAR, 1995);
		startTime.set(Calendar.MONTH, 0);
		startTime.set(Calendar.DAY_OF_MONTH, 1);

		Date startDate = startTime.getTime();
		
		//增加事件
		ArrayList<Calendar> eventDays = new ArrayList<Calendar>();
		Calendar weddingDay = Calendar.getInstance();
		weddingDay.set(2013, 04, 04);
		eventDays.add(weddingDay);
		
		Calendar weddingDay1 = Calendar.getInstance();
		weddingDay1.set(2013, 05, 04);
		eventDays.add(weddingDay1);
		
		Calendar weddingDay6 = Calendar.getInstance();
		weddingDay6.set(2013, 05, 01);
		eventDays.add(weddingDay6);
		
		calendar.init(new Date(), startDate, targetDate.getTime(),
				getSupportFragmentManager(),eventDays);
		calendar.setVerticalScrollBarEnabled(false);
		calendar.setEnabled(false);
		calendar.setOnPageChangeListener(this);
		// 星期部分
		headerRow = (CalendarRowView) findViewById(R.id.weekRow);
		for (int c = Calendar.SUNDAY; c <= Calendar.SATURDAY; c++) {
			targetDate.set(Calendar.DAY_OF_WEEK, c);
			final TextView textView = (TextView) headerRow.getChildAt(c - 1);
			textView.setText(new SimpleDateFormat("EEE").format(targetDate
					.getTime()));
		}
		title = (TextView) findViewById(R.id.title);
		title.setText(new SimpleDateFormat("MMMM yyyy").format(targetDate
				.getTime()));
		calendar.setLayoutParams(new LinearLayout.LayoutParams(calendar
				.getLayoutParams().width, (cellSize * 6)));
		position = calendar.selectedIndex;

		// handle action
		done = (TextView) findViewById(R.id.add_textview);
		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "Selected time in millis: "
						+ calendar.getSelectedDate().getTime());
				Toast.makeText(
						SampleTimesSquareActivity.this,
						new SimpleDateFormat("yyyy.MM.dd").format(calendar
								.getSelectedDate().getTime()), LENGTH_SHORT)
						.show();
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

	// Calendar Provider部分
	@SuppressLint("NewApi")
	public Cursor queryEventsFromTheCalendar(long calID) {
		getContentResolver().query(
				Events.CONTENT_URI,
				new String[] { "_id", "title", "dtstart", "dtend" },
				CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
						+ CalendarContract.Events.DELETED + " != ?",
				new String[] { String.valueOf(calID), "1" }, "dtstart ASC");

		return null;
	}

	@SuppressLint("NewApi")
	public PNCalendar queryCalendar() throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException {
		Cursor cur = null;
		PNCalendar pnCalendar = null;

		ContentResolver cr = getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "(" + Calendars.CALENDAR_DISPLAY_NAME + " = ?)";
		String[] selectionArgs = new String[] { "kevin@bhiner.com" };

		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		pnCalendar = new PNCalendar();
		if (cur.moveToNext()) {
			cursor2PNCalendar(cur, pnCalendar);
		}
		cur.close();
		return pnCalendar;
	}

	// 将Cursor的一行转化成一个PNCalendar
	public PNCalendar cursor2PNCalendar(Cursor cur, PNCalendar pnCalendar)
			throws IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException {
		pnCalendar.set_id(cur.getString(cur.getColumnIndex("_id")));
		pnCalendar.setAccount_name(cur.getString(cur.getColumnIndex("account_name")));
		pnCalendar.setCalendar_displayName(cur.getString(cur.getColumnIndex("calendar_displayName")));
		pnCalendar.setOwnerAccount(cur.getString(cur.getColumnIndex("ownerAccount")));
		return pnCalendar;
	}
}
