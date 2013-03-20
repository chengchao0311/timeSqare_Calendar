package com.squareup.timessquare;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.sample.BuildConfig;
import com.squareup.timessquare.sample.CalendarPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonthFragment extends Fragment {
	List<List<List<MonthCellDescriptor>>> cells;
	List<MonthDescriptor> months;
	DateFormat weekdayNameFormat;
	MonthView.Listener listener;
	Calendar today;
	ArrayList<PNEvent> events;
	int position;
	
	public static MonthFragment create(List<MonthDescriptor> months, 
			List<List<List<MonthCellDescriptor>>> cells
			,MonthView.Listener listener,DateFormat weekDateFormat
			,Calendar today,int position,ArrayList<PNEvent> events
			){
		MonthFragment monthFragment = new MonthFragment();
		monthFragment.cells = cells;
		monthFragment.months = months;
		monthFragment.weekdayNameFormat = weekDateFormat;
		monthFragment.today = today;
		monthFragment.listener = listener;
		monthFragment.position = position;
		monthFragment.events = events;
		return monthFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MonthView monthView = MonthView.create(container, inflater, weekdayNameFormat,
				listener, today);
		if (BuildConfig.DEBUG) {
			
		}
		monthView.init(months.get(position), cells.get(position),events);
		return monthView;
	}
}
