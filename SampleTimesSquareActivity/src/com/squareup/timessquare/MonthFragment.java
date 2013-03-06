package com.squareup.timessquare;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MonthFragment extends Fragment {
	List<List<List<MonthCellDescriptor>>> cells;
	List<MonthDescriptor> months;
	DateFormat weekdayNameFormat;
	MonthView.Listener listener;
	Calendar today;
	int position;
	
	public static MonthFragment create(List<MonthDescriptor> months, 
			List<List<List<MonthCellDescriptor>>> cells
			,MonthView.Listener listener,DateFormat weekDateFormat
			,Calendar today,int position
			){
		MonthFragment monthFragment = new MonthFragment();
		monthFragment.cells = cells;
		monthFragment.months = months;
		monthFragment.weekdayNameFormat = weekDateFormat;
		monthFragment.today = today;
		monthFragment.listener = listener;
		monthFragment.position = position;
		
		return monthFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		MonthView monthView = MonthView.create(container, inflater, weekdayNameFormat,
				listener, today);

		monthView.init(months.get(position), cells.get(position));
		return monthView;
	}
}
