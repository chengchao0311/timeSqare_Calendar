// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import java.util.ArrayList;
import java.util.Date;

import com.pnwedding.domain.PNEvent;

/** Describes the state of a particular date cell in a {@link MonthView}. */
public class MonthCellDescriptor {
	private final Date date;
	private final int value;
	private final boolean isCurrentMonth;
	private boolean isSelected;
	private final boolean isToday;
	private final boolean isSelectable;
	public boolean hasEvent;
	private int monthIndex;
	private int weekIndex;
	private int dayIndex;

	MonthCellDescriptor(Date date, boolean currentMonth, boolean selectable,
			boolean selected, boolean hasEvent, boolean today, int value) {
		this.date = date;
		isCurrentMonth = currentMonth;
		isSelectable = selectable;
		isSelected = selected;
		isToday = today;
		this.hasEvent = hasEvent;
		this.value = value;
	}
	
	public int getMonthIndex() {
		return monthIndex;
	}

	public void setMonthIndex(int monthIndex) {
		this.monthIndex = monthIndex;
	}

	public int getWeekIndex() {
		return weekIndex;
	}

	public void setWeekIndex(int weekIndex) {
		this.weekIndex = weekIndex;
	}

	public int getDayIndex() {
		return dayIndex;
	}

	public void setDayIndex(int dayIndex) {
		this.dayIndex = dayIndex;
	}

	public void setHasEvent(boolean hasEvent) {
		this.hasEvent = hasEvent;
	}


	public Date getDate() {
		return date;
	}

	public boolean isCurrentMonth() {
		return isCurrentMonth;
	}

	public boolean isSelectable() {
		return isSelectable;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean selected) {
		isSelected = selected;
	}

	public boolean isToday() {
		return isToday;
	}

	public int getValue() {
		return value;
	}

	public boolean isHasEvent() {
		return hasEvent;
	}


	@Override
	public String toString() {
		return "MonthCellDescriptor{" + "date=" + date + ", value=" + value
				+ ", isCurrentMonth=" + isCurrentMonth + ", isSelected="
				+ isSelected + ", isToday=" + isToday + ", isSelectable="
				+ isSelectable + '}';
	}
}
