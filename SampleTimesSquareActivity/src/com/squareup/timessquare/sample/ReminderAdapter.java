package com.squareup.timessquare.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pnwedding.domain.ReminderTimeDescriptor;
import com.pnwedding.utils.CheckImageView;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class ReminderAdapter extends ArrayAdapter<ReminderTimeDescriptor>{
	private Activity context;
	private ArrayList<ReminderTimeDescriptor> reminders;
	public int selectedPos = 1200;
	
	public ReminderAdapter(Context context, int textViewResourceId, List<ReminderTimeDescriptor> reminders) {
		super(context, textViewResourceId);
		this.context = (Activity) context;
		this.reminders = (ArrayList<ReminderTimeDescriptor>) reminders;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = context.getLayoutInflater().inflate(R.layout.choose_reminder_item,parent,false);
		TextView textView  = (TextView)view.findViewById(R.id.reminder_time);
		ReminderTimeDescriptor reminderMessage = reminders.get(position);
		textView.setText(reminderMessage.text);
		
		CheckImageView checkBox = (CheckImageView) view.findViewById(R.id.checkBox);
		if (selectedPos != 1200) {//1200表示沒被選擇過
			if (position == selectedPos) {
				checkBox.setChecked(true);
			}else {
				checkBox.setChecked(false);
			}
		}
		return view;
	}

	@Override
	public int getCount() {
		return reminders.size();
	}


	
}
