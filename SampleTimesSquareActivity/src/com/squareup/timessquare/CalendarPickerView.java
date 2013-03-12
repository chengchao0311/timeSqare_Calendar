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
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.sample.R;

/**
 * Android component to allow picking a date from a calendar view (a list of
 * months). Must be initialized after inflation with
 * {@link #init(java.util.Date, java.util.Date, java.util.Date)}. The currently
 * selected date can be retrieved with {@link #getSelectedDate()}.
 */
public class CalendarPickerView extends ViewPager {
	private CalendarPickerView.MonthAdapter adapter;
	private final DateFormat monthNameFormat;
	private final DateFormat weekdayNameFormat;
	private final DateFormat fullDateFormat;
	public final List<MonthDescriptor> months = new ArrayList<MonthDescriptor>();
	public final List<List<List<MonthCellDescriptor>>> cells = new ArrayList<List<List<MonthCellDescriptor>>>();
	public int monthView1Height;

	private MonthCellDescriptor selectedCell;
	final Calendar today = Calendar.getInstance();
	private final Calendar selectedCal = Calendar.getInstance();
	private final Calendar minCal = Calendar.getInstance();
	private final Calendar maxCal = Calendar.getInstance();
	private final Calendar monthCounter = Calendar.getInstance();
	private ArrayList<PNEvent> events;
	private final MonthView.Listener listener = new CellClickedListener();
	public int selectedIndex;
	public ToDoListCallBack toDoListCallBack;

	public CalendarPickerView(Context context, AttributeSet attrs) {
		super(context, attrs);

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
			cells.add(getMonthCells(month, monthCounter, selectedCal));
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

	private void scrollToSelectedMonth(final int selectedIndex) {
		post(new Runnable() {
			@SuppressLint("NewApi")
			@Override
			public void run() {
				setCurrentItem(selectedIndex);
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
	private static void setMidnight(Calendar cal) {
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
				// De-select the currently-selected cell.
				selectedCell.setSelected(false);
				// Select the new cell.
				selectedCell = cell;
				selectedCell.setSelected(true);
				// Track the currently selected date value.
				selectedCal.setTime(cell.getDate());
				// Update the adapter.
				if (cell.isHasEvent()) {
					if (toDoListCallBack != null) {
						toDoListCallBack.showEventsOfTheDay(cell.getEvents());
					}
				} else {
					if (toDoListCallBack != null) {
						toDoListCallBack.showEventsOfTheDay(null);
					}
				}
				adapter.notifyDataSetChanged();
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
					weekDateFormat, today, position);
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
			Calendar startCal, Calendar selectedDate) {
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
			Calendar tempCal = null;
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

				for (int i = 0; i < events.size(); i++) {
					if (tempCal == null) {
						tempCal = Calendar.getInstance();
					}
					tempCal.setTimeInMillis(events.get(i).dtstart);
					
					if (eventTimeInOneDay(events.get(i))) {//当前事件的开始时间和结束时间在同一天内
						if (sameDate(cal, tempCal)) {//当前天和event是否是同一天
							hasEvent = true;
							break;
						} else {
							hasEvent = false;
						}
					} else {// 开始和结束时间不在同一天内 
						if (betweenDates(cal, events.get(i).dtstart, events.get(i).dtend)) {
							hasEvent = true;
							break;
						} else {
							hasEvent = false;
						}
					}
				}
				MonthCellDescriptor cell = null;
				if (hasEvent) {
					ArrayList<PNEvent> sameDayEvents = getEventsOfTheDay(cal
							.getTimeInMillis());
					cell = new MonthCellDescriptor(date, isCurrentMonth,
							isSelectable, isSelected, hasEvent, isToday, value,
							sameDayEvents);
				} else {
					cell = new MonthCellDescriptor(date, isCurrentMonth,
							isSelectable, isSelected, hasEvent, isToday, value,
							null);
				}

				if (isSelected) {
					selectedCell = cell;
				}
				weekCells.add(cell);
				cal.add(DATE, 1);
			}
		}
		return cells;
	}
	//比较时间再区间内
	public boolean checkHasEvents(Calendar cal, long startMill, long endMill) {
		if ((cal.getTimeInMillis() >= startMill) && (cal.getTimeInMillis() <= endMill)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean eventTimeInOneDay(PNEvent pnEvent){
		Calendar aCal = Calendar.getInstance();
		Calendar bCal = Calendar.getInstance();
		aCal.setTimeInMillis(pnEvent.dtstart);
		bCal.setTimeInMillis(pnEvent.dtend);
		return sameDate(aCal, bCal);
	}
	
	// 获得time 所在时间的所有event
	private ArrayList<PNEvent> getEventsOfTheDay(long time) {
		ArrayList<PNEvent> oneDayEvents = new ArrayList<PNEvent>();
		for (int i = 0; i < events.size(); i++) {
			if (eventTimeInOneDay(events.get(i))) { 
				Calendar currCal = Calendar.getInstance();
				Calendar eventCal = Calendar.getInstance();
				currCal.setTimeInMillis(time);
				eventCal.setTimeInMillis(events.get(i).dtstart);
				if (sameDate(eventCal, currCal)) {
					oneDayEvents.add(events.get(i));
				}
			}else {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(time);
				if (betweenDates(cal, events.get(i).dtstart, events.get(i).dtend)) { //时间的区间包含这一天
					oneDayEvents.add(events.get(i));
				}
			}
		}
		return oneDayEvents;
	}

	private static boolean sameDate(Calendar cal, Calendar selectedDate) {
		return cal.get(MONTH) == selectedDate.get(MONTH)
				&& cal.get(YEAR) == selectedDate.get(YEAR)
				&& cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
	}
	
	private static boolean betweenDates(Calendar cal, long minCalLong,
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
}