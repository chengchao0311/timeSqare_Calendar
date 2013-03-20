package com.pnwedding.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.util.Log;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.MonthCellDescriptor;
import com.squareup.timessquare.sample.BuildConfig;
public class PNCalendar implements Parcelable {

	public long _id;
	public String account_name;
	public String calendar_displayName;
	public String ownerAccount;

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getAccount_name() {
		return account_name;
	}

	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}

	public String getCalendar_displayName() {
		return calendar_displayName;
	}

	public void setCalendar_displayName(String calendar_displayName) {
		this.calendar_displayName = calendar_displayName;
	}

	public String getOwnerAccount() {
		return ownerAccount;
	}

	public void setOwnerAccount(String ownerAccount) {
		this.ownerAccount = ownerAccount;
	}

	// --------------------------------------------------------------------------

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// 1.必须按成员变量声明的顺序封装数据，不然会出现获取数据出错
		// 2.序列化对象
		dest.writeLong(_id);
		dest.writeString(account_name);
		dest.writeString(calendar_displayName);
		dest.writeString(ownerAccount);
	}

	public static final Parcelable.Creator<PNCalendar> CREATOR = new Parcelable.Creator<PNCalendar>() {

		@Override
		public PNCalendar createFromParcel(Parcel source) {
			// 必须按成员变量声明的顺序读取数据，不然会出现获取数据出错
			// 反序列化對象
			PNCalendar pnCalendar = new PNCalendar();
			pnCalendar.set_id(source.readLong());
			pnCalendar.setAccount_name(source.readString());
			pnCalendar.setCalendar_displayName(source.readString());
			pnCalendar.setOwnerAccount(source.readString());
			return pnCalendar;
		}

		@Override
		public PNCalendar[] newArray(int size) {
			return new PNCalendar[size];
		}
	};

	// --------------------------------------------------------------------------
	@SuppressLint("NewApi")
	public static PNCalendar findPNCalendarByDisplayName(Context context,
			String displayName) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException {
		Cursor cur = null;
		PNCalendar pnCalendar = null;

		ContentResolver cr = context.getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "(" + Calendars.CALENDAR_DISPLAY_NAME + " = ?)";
		String[] selectionArgs = new String[] { displayName };

		cur = cr.query(uri, new String[] { Calendars._ID, // 0
				Calendars.ACCOUNT_NAME, // 1
				Calendars.CALENDAR_DISPLAY_NAME, // 2
				Calendars.OWNER_ACCOUNT // 3
				}, selection, selectionArgs, null);
		pnCalendar = new PNCalendar();
		if (cur.moveToNext()) {
			cursor2PNCalendar(cur, pnCalendar);
		}
		cur.close();
		return pnCalendar;
	}

	// 将Cursor的一行转化成一个PNCalendar
	private static PNCalendar cursor2PNCalendar(Cursor cur,
			PNCalendar pnCalendar) throws IllegalArgumentException,
			IllegalAccessException, NoSuchFieldException {
		pnCalendar.set_id(cur.getLong(cur.getColumnIndex("_id")));
		pnCalendar.setAccount_name(cur.getString(cur
				.getColumnIndex("account_name")));
		pnCalendar.setCalendar_displayName(cur.getString(cur
				.getColumnIndex("calendar_displayName")));
		pnCalendar.setOwnerAccount(cur.getString(cur
				.getColumnIndex("ownerAccount")));
		return pnCalendar;
	}

	@SuppressLint("NewApi")
	public ArrayList<PNEvent> queryEventsFromCalendar(final Context context,final ArrayList<PNEvent> events,final CalendarPickerView calendar, final Handler hadnler) {
		// CalendarContract.Events.CALENDAR_ID != 1 表示只查询未被删除的事件
		ContentResolver cr = context.getContentResolver();
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(context.getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				while (cursor.moveToNext()) {
					PNEvent pnEvent = new PNEvent();
					pnEvent.calendar_id = PNCalendar.this._id;
					pnEvent.title = cursor.getString(cursor.getColumnIndex("title"));
					pnEvent.dtstart = cursor.getLong(cursor.getColumnIndex("dtstart"));
					pnEvent.dtend = cursor.getLong(cursor.getColumnIndex("dtend"));
					pnEvent._id = cursor.getLong(cursor.getColumnIndex("_id"));
					pnEvent.description = cursor.getString(cursor
							.getColumnIndex("description"));
					events.add(pnEvent);
				}
				cursor.close();
				
//				if (events.size() != 0) {
//					for (int i = 0; i < events.size(); i++) {
//						PNEvent pnEvent2 = events.get(i);
//						ArrayList<MonthCellDescriptor> cells = calendar.findMonthCellByEvent(pnEvent2);//找到與事件相關的cell
//						if (cells.size() == 1) {//說明開始結束是在一天
//							SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy MM dd");
//							MonthCellDescriptor monthCellDescriptor = cells.get(0);
//							
//							if (BuildConfig.DEBUG) {
//								System.err.println("events.size()" + events.size());
//								System.out.println("更新的 cell date"  + simpleDateFormat.format(cells.get(0).getDate()));
//								long time = monthCellDescriptor.getDate().getTime();
//							}
//							
//							//先設置成有事件
//							if (!monthCellDescriptor.isHasEvent()) {
//								monthCellDescriptor.hasEvent = true;
//							}
//							//增加事件
//							ArrayList<PNEvent> oneDayevents = monthCellDescriptor.getEvents();
//							if (oneDayevents == null) {//如果沒有eventlist
//								oneDayevents = new ArrayList<PNEvent>();
//								oneDayevents.add(pnEvent2);
//								monthCellDescriptor.setEvents(oneDayevents);
//							}else {
//								oneDayevents.add(pnEvent2);
//							}
//							
//							
//						}	
//					}
//				}
				hadnler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						Log.e("event", "刷新");
						calendar.getAdapter().notifyDataSetChanged();
					}
				}, 500);

			}
		};
		
		queryHandler.startQuery(111, new Object(), Events.CONTENT_URI, new String[] { "_id", "title", "dtstart", "dtend",
						"description" },CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
						+ CalendarContract.Events.DELETED + " != ?", new String[] { String.valueOf(PNCalendar.this._id), "1" }, "dtstart ASC");
		
//		Cursor cur = .query(
//				Events.CONTENT_URI,
//				new String[] { "_id", "title", "dtstart", "dtend",
//						"description" },
//				CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
//						+ CalendarContract.Events.DELETED + " != ?",
//				new String[] { String.valueOf(PNCalendar.this._id), "1" },
//				"dtstart ASC");

		// String[] l = cur.getColumnNames();

	
		return events;
	}

	@SuppressLint("NewApi")
	public long insertEvent(Context context, String title, String description,
			Calendar start, Calendar end, String hasAlarm) {
		try {
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			// values
			values.put(CalendarContract.Events.CALENDAR_ID, this._id);
			values.put(CalendarContract.Events.TITLE, title);
			values.put(CalendarContract.Events.DESCRIPTION, description);
			values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone
					.getDefault().getID());
			values.put(CalendarContract.Events.DTSTART, start.getTimeInMillis());
			values.put(CalendarContract.Events.DTEND, end.getTimeInMillis());
			values.put(CalendarContract.Events.ALL_DAY, "0");
			values.put(CalendarContract.Events.HAS_ALARM, hasAlarm);
			// Uri,long_id
			Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
			long id = Long.parseLong(uri.getLastPathSegment());

			return id;
		} catch (Exception e) {
			return -1;
		}
	}

	@SuppressLint("NewApi")
	public boolean deleteEvent(Context context, long event_id) {
		try {
			ContentResolver cr = context.getContentResolver();
			// Uri,delete
			Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, event_id);
			int rows = cr.delete(uri, null, null);
			if (rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressLint({ "InlinedApi", "NewApi" })
	public boolean updateEvent(Context context, long event_id, long cal_id,
			String timezone, String title, String description, String place,
			Calendar start, Calendar end, String hasAlarm, String allDay) {
		try {
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			// values
			if (cal_id != -1)
				values.put(Events.CALENDAR_ID, cal_id);
			if (title != null)
				values.put(Events.TITLE, title);
			if (description != null)
				values.put(Events.DESCRIPTION, description);
			if (place != null)
				values.put(Events.EVENT_LOCATION, place);
			if (timezone != null)
				values.put(Events.EVENT_TIMEZONE, timezone);
			if (start != null)
				values.put(Events.DTSTART, start.getTimeInMillis());
			if (end != null)
				values.put(Events.DTEND, end.getTimeInMillis());
			if (allDay != null)
				values.put(Events.ALL_DAY, allDay);
			if (hasAlarm != null)
				values.put(Events.HAS_ALARM, hasAlarm);
			// Uri,long_id
			String where = Events._ID + "=?";
			String[] selectionArgs = new String[] { String.valueOf(event_id) };
			int rows = cr.update(Events.CONTENT_URI, values, where,
					selectionArgs);
			if (rows > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
