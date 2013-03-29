package com.squareup.timessquare.sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.pnwedding.domain.ReminderTimeDescriptor;
import com.pnwedding.utils.CheckImageView;
import com.pnwedding.utils.ReminderTimeDescriptorComPara;
import com.pnwedding.utils.Utils;

public class ChooseReminder extends Activity implements OnItemClickListener {
	private ListView listView;
	private ReminderAdapter reminderAdapter;
	private ArrayList<ReminderTimeDescriptor> reminders;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_reminder);
		listView = (ListView) findViewById(R.id.listview);
		reminders = new ArrayList<ReminderTimeDescriptor>();
		
		Bundle extras = getIntent().getExtras();
		long reminderTimeMill = extras.getLong("reminder_timeMill");
		
		//生成item
		Set<String> stringPropertyNames = Utils.getReminderProperties()
				.stringPropertyNames();
		for (String string : stringPropertyNames) {
			ReminderTimeDescriptor reminderTimeDescriptor = new ReminderTimeDescriptor();
			reminderTimeDescriptor.setText(string);
			String longString = Utils.getReminderProperties().getProperty(
					string);
			long mlong = Long.parseLong(longString);
			reminderTimeDescriptor.setTimeMills(mlong);
			reminders.add(reminderTimeDescriptor);
		}
		//排序
		Collections.sort(reminders, new ReminderTimeDescriptorComPara());
		reminderAdapter = new ReminderAdapter(this, R.layout.choose_reminder,
				reminders);
		//遍历 找到默认位置
		for (int j = 0; j < reminders.size(); j++) {
			ReminderTimeDescriptor rtd = reminders.get(j);
			if (rtd.timeMills == reminderTimeMill) {
				reminderAdapter.selectedPos = j;
			}
		}
		
		listView.setAdapter(reminderAdapter);
		listView.setOnItemClickListener(this);
	}

	public void back(View view) {
		finish();
	}

	public void done(View view) {
		Intent intent = new Intent();
		intent.putExtra("reminderTimeDescriptor",
				reminders.get(reminderAdapter.selectedPos));
		setResult(CalendarPage.EVENTDETAIL_CHOOSEREMINDER, intent);
		finish();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
		reminderAdapter.selectedPos = position;
		CheckImageView checkBox = (CheckImageView) v.findViewById(R.id.checkBox);
		if (checkBox.checked) {
			checkBox.setChecked(false);
		} else {
			checkBox.setChecked(true);
		}
		reminderAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("ChooseReminder onDestroy");
	}

	// *********************************************//
	// ********************作廢***********************//
	// *********************************************//

	// private long[] reminderTime = { 60L * 24L * 90L, 60L * 24L * 30L,
	// 60L * 24L * 3L, 60L * 24L, 60L * 3, 0 };

	// reminders.add(new
	// ReminderTimeDescriptor().setText("三個月前").setTimeMills(reminderTime[0]));
	// reminders.add(new
	// ReminderTimeDescriptor().setText("一個月前").setTimeMills(reminderTime[1]));
	// reminders.add(new
	// ReminderTimeDescriptor().setText("三天前").setTimeMills(reminderTime[2]));
	// reminders.add(new
	// ReminderTimeDescriptor().setText("一天前").setTimeMills(reminderTime[3]));
	// reminders.add(new
	// ReminderTimeDescriptor().setText("三小時前").setTimeMills(reminderTime[4]));
	// reminders.add(new
	// ReminderTimeDescriptor().setText("無").setTimeMills(reminderTime[0]));

}
