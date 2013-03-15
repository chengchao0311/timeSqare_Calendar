// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

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

		view.grid
				.getChildAt(0);
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
			List<List<MonthCellDescriptor>> cells) {
		Logr.d("Initializing MonthView for %s", month);
		long start = System.currentTimeMillis();

		final int numRows = cells.size();
		for (int i = 0; i < 6; i++) {
			CalendarRowView weekRow = (CalendarRowView) grid.getChildAt(i + 1);
			weekRow.setListener(listener);
			if (i < numRows) {
				weekRow.setVisibility(VISIBLE);
				List<MonthCellDescriptor> week = cells.get(i);
				for (int c = 0; c < week.size(); c++) {
					MonthCellDescriptor cell = week.get(c);
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
		return month.getLabel();
	}

	public interface Listener {
		void handleClick(MonthCellDescriptor cell);
	}

}
