package com.squareup.timessquare.sample;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.pnwedding.domain.ReminderTimeDescriptor;
import com.pnwedding.utils.CheckImageView;


public class ChooseReminder extends Activity implements OnItemClickListener {
	private ListView listView;
	private ReminderAdapter reminderAdapter;
	private ArrayList<ReminderTimeDescriptor> reminders;
	private long[] reminderTime = {60L*24L*90L, 60L*24L*30L, 60L*24L*3L, 60L*24L, 60L*3};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_reminder);
		listView = (ListView) findViewById(R.id.listview);
		reminders = new ArrayList<ReminderTimeDescriptor>();
		reminders.add(new ReminderTimeDescriptor().setText("三個月前").setTimeMills(reminderTime[0]));
		reminders.add(new ReminderTimeDescriptor().setText("一個月前").setTimeMills(reminderTime[1]));
		reminders.add(new ReminderTimeDescriptor().setText("三天前").setTimeMills(reminderTime[2]));
		reminders.add(new ReminderTimeDescriptor().setText("一天前").setTimeMills(reminderTime[3]));
		reminders.add(new ReminderTimeDescriptor().setText("三小時前").setTimeMills(reminderTime[4]));
		reminderAdapter = new ReminderAdapter(this, R.layout.choose_reminder,reminders);
		listView.setAdapter(reminderAdapter);
		listView.setOnItemClickListener(this);
	}
	
	public void back(View view) {
		finish();
	}
	
	public void done(View view){
		Intent intent = new Intent();
		intent.putExtra("reminderTimeDescriptor", reminders.get(reminderAdapter.selectedPos));
		setResult(CalendarPage.EVENTDETAIL_CHOOSEREMINDER, intent);
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		reminderAdapter.selectedPos = position;
		CheckImageView checkBox = (CheckImageView) v.findViewById(R.id.checkBox);
		if (checkBox.checked) {
			checkBox.setChecked(false);
		}else {
			checkBox.setChecked(true);
		}
		reminderAdapter.notifyDataSetChanged();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("ChooseReminder onDestroy");
	}
	
}
