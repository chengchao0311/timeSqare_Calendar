package com.squareup.timessquare;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.SUNDAY;
import static java.util.Calendar.YEAR;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;
import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.sample.BuildConfig;
import com.squareup.timessquare.sample.R;
import com.squareup.timessquare.sample.ToDoListCallBack;

public class CalendarPickerView extends ViewPager {
	private CalendarPickerView.MonthAdapter adapter;
	private final DateFormat monthNameFormat;
	private final DateFormat weekdayNameFormat;
	private final DateFormat fullDateFormat;
	public final List<MonthDescriptor> months = new ArrayList<MonthDescriptor>();
	public final List<List<List<MonthCellDescriptor>>> cells = new ArrayList<List<List<MonthCellDescriptor>>>();
	public int monthView1Height;

	public MonthCellDescriptor selectedCell;
	final Calendar today = Calendar.getInstance();
	private final Calendar selectedCal = Calendar.getInstance();
	private final Calendar minCal = Calendar.getInstance();
	private final Calendar maxCal = Calendar.getInstance();
	private final Calendar monthCounter = Calendar.getInstance();
	private ArrayList<PNEvent> events;
	public HashMap<Long, Integer[]> indexHelper = new HashMap<Long, Integer[]>();
	private final MonthView.Listener listener = new CellClickedListener();
	public int selectedIndex;
	public ToDoListCallBack toDoListCallBack;

	@SuppressLint("SimpleDateFormat")
	public CalendarPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		@SuppressWarnings("unused")
		final int bg = context.getResources().getColor(R.color.calendar_bg);
		monthNameFormat = new SimpleDateFormat(
				context.getString(R.string.month_name_format));
		weekdayNameFormat = new SimpleDateFormat(
				context.getString(R.string.day_name_format));
		fullDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
	}

	/**
	 * All date parameters must be non-null and their
	 * {@link java.util.Date#getTime()} must not return 0. Time of day will be
	 * ignored. For instance, if you pass in {@code minDate} as 11/16/2012
	 * 5:15pm and {@code maxDate} as 11/16/2013 4:30am, 11/16/2012 will be the
	 * first selectable date and 11/15/2013 will be the last selectable date (
	 * {@code maxDate} is exclusive).
	 * 
	 * @param selectedDate
	 *            Initially selected date. Must be between {@code minDate} and
	 *            {@code maxDate}.
	 * @param minDate
	 *            Earliest selectable date, inclusive. Must be earlier than
	 *            {@code maxDate}.
	 * @param maxDate
	 *            Latest selectable date, exclusive. Must be later than
	 *            {@code minDate}.
	 */
	public void init(Date selectedDate, Date minDate, Date maxDate,
			FragmentManager fm, ArrayList<PNEvent> events) {
		adapter = new MonthAdapter(fm, months, cells, listener,
				weekdayNameFormat, today);
		setAdapter(adapter);
		this.events = events;
		if (selectedDate == null || minDate == null || maxDate == null) {
			throw new IllegalArgumentException("All dates must be non-null.  "
					+ dbg(selectedDate, minDate, maxDate));
		}
		if (selectedDate.getTime() == 0 || minDate.getTime() == 0
				|| maxDate.getTime() == 0) {
			throw new IllegalArgumentException("All dates must be non-zero.  "
					+ dbg(selectedDate, minDate, maxDate));
		}
		if (minDate.after(maxDate)) {
			throw new IllegalArgumentException(
					"Min date must be before max date.  "
							+ dbg(selectedDate, minDate, maxDate));
		}
		if (selectedDate.before(minDate) || selectedDate.after(maxDate)) {
			throw new IllegalArgumentException(
					"selectedDate must be between minDate and maxDate.  "
							+ dbg(selectedDate, minDate, maxDate));
		}

		// Clear previous state.
		cells.clear();
		months.clear();

		// Sanitize input: clear out the hours/minutes/seconds/millis.
		selectedCal.setTime(selectedDate);
		minCal.setTime(minDate);
		maxCal.setTime(maxDate);
		setMidnight(selectedCal);
		setMidnight(minCal);
		setMidnight(maxCal);
		// maxDate is exclusive: bump back to the previous day so if maxDate is
		// the first of a month,
		// we don't accidentally include that month in the view.
		maxCal.add(MINUTE, -1);

		// Now iterate between minCal and maxCal and build up our list of months
		// to show.
		monthCounter.setTime(minCal.getTime());
		final int maxMonth = maxCal.get(MONTH);
		final int maxYear = maxCal.get(YEAR);
		final int selectedYear = selectedCal.get(YEAR);
		final int selectedMonth = selectedCal.get(MONTH);
		selectedIndex = 0;
		while ((monthCounter.get(MONTH) <= maxMonth // Up to, including the
													// month.
				|| monthCounter.get(YEAR) < maxYear) // Up to the year.
				&& monthCounter.get(YEAR) < maxYear + 1) { // But not > next yr.
			MonthDescriptor month = new MonthDescriptor(
					monthCounter.get(MONTH), monthCounter.get(YEAR),
					monthNameFormat.format(monthCounter.getTime()));
			cells.add(getMonthCells(month, monthCounter, selectedCal,
					months.size()));// month size 本月加入后的position
			Logr.d("Adding month %s", month);
			if (selectedMonth == month.getMonth()
					&& selectedYear == month.getYear()) {
				selectedIndex = months.size();
			}
			months.add(month);
			monthCounter.add(MONTH, 1);
		}
		adapter.notifyDataSetChanged();
		if (selectedIndex != 0) {
			scrollToSelectedMonth(selectedIndex);
		}
	}
	
	public void scrollToSelectedMonth(final int selectedIndex) {
		post(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {
				setCurrentItem(selectedIndex, true);
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (months.isEmpty()) {
			throw new IllegalStateException(
					"Must have at least one month to display.  Did you forget to call init()?");
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public Date getSelectedDate() {
		return selectedCal.getTime();
	}

	/** Returns a string summarizing what the client sent us for init() params. */
	private static String dbg(Date startDate, Date minDate, Date maxDate) {
		return "startDate: " + startDate + "\nminDate: " + minDate
				+ "\nmaxDate: " + maxDate;
	}

	/** Clears out the hours/minutes/seconds/millis of a Calendar. */
	public static void setMidnight(Calendar cal) {
		cal.set(HOUR_OF_DAY, 0);
		cal.set(MINUTE, 0);
		cal.set(SECOND, 0);
		cal.set(MILLISECOND, 0);
	}

	private class CellClickedListener implements MonthView.Listener {
		@Override
		public void handleClick(MonthCellDescriptor cell) {
			if (!betweenDates(cell.getDate(), minCal, maxCal)) {
				String errMessage = getResources().getString(
						R.string.invalid_date,
						fullDateFormat.format(minCal.getTime()),
						fullDateFormat.format(maxCal.getTime()));
				Toast.makeText(getContext(), errMessage, Toast.LENGTH_SHORT)
						.show();
			} else {
				setSelectedCell(cell);
				adapter.notifyDataSetChanged();
				// 刷新toDoList
				if (cell.isHasEvent()) {
					toDoListCallBack.showEventsForTheDay();
				} else {
					toDoListCallBack.clearAndrefresh();
				}
			}
		}
	}

	private class MonthAdapter extends FragmentPagerAdapter {
		List<MonthDescriptor> months;
		List<List<List<MonthCellDescriptor>>> cells;
		MonthView.Listener listener;
		DateFormat weekDateFormat;
		Calendar today;

		public MonthAdapter(FragmentManager fm, List<MonthDescriptor> months,
				List<List<List<MonthCellDescriptor>>> cells,
				MonthView.Listener Listener, DateFormat weekDateFormat,
				Calendar today) {
			super(fm);
			this.months = months;
			this.cells = cells;
			this.listener = Listener;
			this.weekDateFormat = weekDateFormat;
			this.today = today;
		}

		@Override
		public int getCount() {
			return months.size();
		}

		@Override
		public Fragment getItem(int position) {
			return MonthFragment.create(months, cells, listener,
					weekDateFormat, today, position, events);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

	}

	List<List<MonthCellDescriptor>> getMonthCells(MonthDescriptor month,
			Calendar startCal, Calendar selectedDate, int monthIndex) {
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MM dd HH mm ss");
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(startCal.getTime());
		List<List<MonthCellDescriptor>> cells = new ArrayList<List<MonthCellDescriptor>>();
		cal.set(DAY_OF_MONTH, 1);
		int firstDayOfWeek = cal.get(DAY_OF_WEEK);
		cal.add(DATE, SUNDAY - firstDayOfWeek);
		while ((cal.get(MONTH) < month.getMonth() + 1 || cal.get(YEAR) < month
				.getYear()) //
				&& cal.get(YEAR) <= month.getYear()) {
			Logr.d("Building week row starting at %s", cal.getTime());
			List<MonthCellDescriptor> weekCells = new ArrayList<MonthCellDescriptor>();
			cells.add(weekCells);
			int weekIndex = cells.size() - 1;// 表示week的位置
			for (int c = 0; c < 7; c++) {
				Date date = cal.getTime();
				boolean isCurrentMonth = cal.get(MONTH) == month.getMonth();
				boolean isSelected = isCurrentMonth
						&& sameDate(cal, selectedDate);
				boolean isSelectable = isCurrentMonth
						&& betweenDates(cal, minCal, maxCal);
				boolean isToday = sameDate(cal, today);
				int value = cal.get(DAY_OF_MONTH);
				boolean hasEvent = false;
				MonthCellDescriptor cell = null;
				cell = new MonthCellDescriptor(date, isCurrentMonth,
						isSelectable, isSelected, hasEvent, isToday, value);
				//為cell 設置索引 
				cell.setMonthIndex(monthIndex);
				cell.setWeekIndex(weekIndex);
				cell.setDayIndex(c);
				if (isSelectable) {
					indexHelper.put(cell.getDate().getTime(), new Integer[] {
						monthIndex, weekIndex, c });
				}
//				Log.e("cell time", simpleDateFormat.format(cell.getDate()));
				if (isSelected) {
					selectedCell = cell;
				}
				weekCells.add(cell);
				cal.add(DATE, 1);
			}
		}
		return cells;
	}



	// *****************************************************************//
	// ***********************有關時間比較的方法****************************//
	// *****************************************************************//
	public static boolean sameDate(Calendar cal, Calendar selectedDate) {
		return cal.get(MONTH) == selectedDate.get(MONTH)
				&& cal.get(YEAR) == selectedDate.get(YEAR)
				&& cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
	}

	public static boolean betweenDates(Calendar cal, long minCalLong,
			long maxCalLong) {
		final Date date = cal.getTime();
		Calendar minCal = Calendar.getInstance();
		minCal.setTimeInMillis(minCalLong);
		Calendar maxCal = Calendar.getInstance();
		maxCal.setTimeInMillis(maxCalLong);
		return betweenDates(date, minCal, maxCal);
	}

	private static boolean betweenDates(Calendar cal, Calendar minCal,
			Calendar maxCal) {
		final Date date = cal.getTime();
		return betweenDates(date, minCal, maxCal);
	}

	static boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
		final Date min = minCal.getTime();
		return (date.equals(min) || date.after(min)) // >= minCal
				&& date.before(maxCal.getTime()); // && < maxCal
	}


	// *****************************************************************//
	// ****************************操作cell方法***************************//
	// *****************************************************************//
	public MonthCellDescriptor getCellByDate(Date dayMidNight){
		MonthCellDescriptor monthCellDescriptor  = null;
		Integer[] indexs = indexHelper.get(dayMidNight.getTime());
		if (indexs != null) {
			monthCellDescriptor = cells.get(indexs[0]).get(indexs[1]).get(indexs[2]);
		}
		return monthCellDescriptor;
	}
	
	public void setSelectedCell(MonthCellDescriptor cell){
		//刷新CalendarPickView
		// De-select the currently-selected cell.
		selectedCell.setSelected(false);
		// Select the new cell.
		selectedCell = cell;
		selectedCell.setSelected(true);
		// Track the currently selected date value.
		Date date2 = cell.getDate();
		selectedCal.setTime(date2);
	}

	// *****************************************************************//
	// ***************************** 作廢方法****************************//
	// *****************************************************************//
	
	
	
	// public ArrayList<MonthCellDescriptor> findMonthCellByEvent(PNEvent
	// pnEvent) {
	// ArrayList<MonthCellDescriptor> cellList = new
	// ArrayList<MonthCellDescriptor>();
	// // 當cell的date在event開始結束之間時就加入這個list
	// // 1,event的開始結束時間再同一天的情況
	// Calendar minCal = Calendar.getInstance();
	// Calendar maxCal = Calendar.getInstance();
	// minCal.setTimeInMillis(pnEvent.dtstart);
	// maxCal.setTimeInMillis(pnEvent.dtend);
	// if (sameDate(minCal, maxCal)) {
	// setMidnight(minCal);
	// Integer[] indexs = indexHelper.get(minCal.getTimeInMillis());
	// if (indexs != null) {
	// for (int i = 0; i < indexs.length; i++) {
	// System.out.println("indexs" + "[" + i + "]" + "="
	// + indexs[i]);
	// }
	// MonthCellDescriptor cell = cells.get(indexs[0]).get(indexs[1])
	// .get(indexs[2]);
	// cellList.add(cell);
	// }
	// } else {// 如果是跨幾天的情況
	// // 暫時什麼都不做
	// }
	//
	// return cellList;
	// }
}