// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import android.content.Context;
import android.graphics.Color;
import android.nfc.Tag;
import android.renderscript.Int2;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.sample.BuildConfig;
import com.squareup.timessquare.sample.CalendarPage;
import com.squareup.timessquare.sample.EventDetail;
import com.squareup.timessquare.sample.R;

public class MonthView extends LinearLayout {
	// private TextView title;
	private CalendarGridView grid;
	private Listener listener;

	public static MonthView create(ViewGroup parent, LayoutInflater inflater,
			DateFormat weekdayNameFormat, Listener listener, Calendar today) {
		final MonthView view = (MonthView) inflater.inflate(R.layout.month,
				parent, false);

		final int originalDayOfWeek = today.get(Calendar.DAY_OF_WEEK);

		view.grid.getChildAt(0);
		for (int c = Calendar.SUNDAY; c <= Calendar.SATURDAY; c++) {
			today.set(Calendar.DAY_OF_WEEK, c);
			// final TextView textView = (TextView) headerRow.getChildAt(c - 1);
			// textView.setText(weekdayNameFormat.format(today.getTime()));
		}
		today.set(Calendar.DAY_OF_WEEK, originalDayOfWeek);
		view.listener = listener;
		return view;
	}

	public MonthView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		grid = (CalendarGridView) findViewById(R.id.calendar_grid);
	}

	public String init(MonthDescriptor month,
			List<List<MonthCellDescriptor>> cells, ArrayList<PNEvent> events) {
		Logr.d("Initializing MonthView for %s", month);
		Calendar tempCal = Calendar.getInstance();
		Calendar minCal = Calendar.getInstance();
		Calendar maxCal = Calendar.getInstance();
		long start = System.currentTimeMillis();
		final int numRows = cells.size();
		for (int i = 0; i < 6; i++) {
			CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
			weekRow.setListener(listener);
			if (i < numRows) {
				if (BuildConfig.DEBUG) {
					if (!CalendarPage.tag) {
						Log.e("numRow", i + "");
						if (i == (numRows - 1)) {
							Log.e("numRow", "最後一行" + numRows);
						}
					}
				}

				weekRow.setVisibility(VISIBLE);
				List<MonthCellDescriptor> week = cells.get(i);

				for (int c = 0; c < week.size(); c++) {
					MonthCellDescriptor cell = week.get(c);

					//檢查是否有事件
					tempCal.setTimeInMillis(cell.getDate().getTime());
					int eventSize = events.size();
					for (int x = 0; x < eventSize; x++) {
						PNEvent pnEvent = events.get(x);
						minCal.setTimeInMillis(pnEvent.dtstart);
						maxCal.setTimeInMillis(pnEvent.dtend);
						if (sameDate(tempCal, minCal) || betweenDates(cell.getDate(),
								minCal, maxCal)) {
							cell.hasEvent = true;
							break;
						}
					}

					// 設置index方便在外部找到
					CheckedTextView cellView = (CheckedTextView) weekRow
							.getChildAt(c);
					cellView.setText(Integer.toString(cell.getValue()));
					cellView.setEnabled(cell.isCurrentMonth());
					cellView.setChecked(!cell.isToday());
					cellView.setSelected(cell.isSelected());
					if (cell.isSelectable()) {
						cellView.setTextColor(getResources().getColorStateList(
								R.color.calendar_text_selector));
					} else {
						cellView.setTextColor(getResources().getColor(
								R.color.calendar_text_unselectable));
					}
					if (cell.isHasEvent()) {
						if (cell.isCurrentMonth()) {
//							if (BuildConfig.DEBUG) {
//								SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
//										"yyyy MM dd");
//								System.out.println("cell data = "
//										+ simpleDateFormat.format(cell
//												.getDate()));
//								System.err.println("cell.getEvents().size"
//										+ cell.getEvents().size());
//							}
							cellView.setTextColor(Color.RED);
						}
					}
					cellView.setTag(cell);
				}
			} else {
				weekRow.setVisibility(GONE);
			}
		}
		Logr.d("MonthView.init took %d ms", System.currentTimeMillis() - start);
		CalendarPage.tag = true;
		return month.getLabel();
	}

	public interface Listener {
		void handleClick(MonthCellDescriptor cell);
	}

	//*************工具
	boolean sameDate(Calendar cal, Calendar selectedDate) {
		return cal.get(MONTH) == selectedDate.get(MONTH)
				&& cal.get(YEAR) == selectedDate.get(YEAR)
				&& cal.get(DAY_OF_MONTH) == selectedDate.get(DAY_OF_MONTH);
	}
	
	boolean betweenDates(Date date, Calendar minCal, Calendar maxCal) {
		final Date min = minCal.getTime();
		return (date.equals(min) || date.after(min)) // >= minCal
				&& date.before(maxCal.getTime()); // && < maxCal
	}
}
