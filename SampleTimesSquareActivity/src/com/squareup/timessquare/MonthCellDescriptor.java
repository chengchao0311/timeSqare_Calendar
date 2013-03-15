// Copyright 2012 Square, Inc.
package com.squareup.timessquare;

import java.util.ArrayList;
import java.util.Date;

import com.pnwedding.domain.PNEvent;

/** Describes the state of a particular date cell in a {@link MonthView}. */
class MonthCellDescriptor {
	private final Date date;
	private final int value;
	private final boolean isCurrentMonth;
	private boolean isSelected;
	private final boolean isToday;
	private final boolean isSelectable;
	private final boolean hasEvent;
	private ArrayList<PNEvent> events;

	MonthCellDescriptor(Date date, boolean currentMonth, boolean selectable,
			boolean selected, boolean hasEvent, boolean today, int value, ArrayList<PNEvent> events) {
		this.date = date;
		isCurrentMonth = currentMonth;
		isSelectable = selectable;
		isSelected = selected;
		isToday = today;
		this.hasEvent = hasEvent;
		this.value = value;
		this.events = events;
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

	public ArrayList<PNEvent> getEvents() {
		return events;
	}

	public void setEvents(ArrayList<PNEvent> events) {
		this.events = events;
	}

	@Override
	public String toString() {
		return "MonthCellDescriptor{" + "date=" + date + ", value=" + value
				+ ", isCurrentMonth=" + isCurrentMonth + ", isSelected="
				+ isSelected + ", isToday=" + isToday + ", isSelectable="
				+ isSelectable + '}';
	}
}
