package com.pnwedding.domain;

import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;

public class PNCalendar {
	@SuppressLint("InlinedApi")
	public static final String[] EVENT_PROJECTION = new String[] {
			Calendars._ID, // 0
			Calendars.ACCOUNT_NAME, // 1
			Calendars.CALENDAR_DISPLAY_NAME, // 2
			Calendars.OWNER_ACCOUNT // 3
	};

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

	@SuppressLint("NewApi")
	public static PNCalendar findPNCalendarByDisplayName(Context context,
			String displayName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
		Cursor cur = null;
		PNCalendar pnCalendar = null;

		ContentResolver cr = context.getContentResolver();
		Uri uri = Calendars.CONTENT_URI;
		String selection = "(" + Calendars.CALENDAR_DISPLAY_NAME + " = ?)";
		String[] selectionArgs = new String[] { displayName };

		cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
		pnCalendar = new PNCalendar();
		if (cur.moveToNext()) {
			cursor2PNCalendar(cur, pnCalendar);
		}
		cur.close();
		return pnCalendar;
	}

	// 将Cursor的一行转化成一个PNCalendar
	private static PNCalendar cursor2PNCalendar(Cursor cur, PNCalendar pnCalendar)
			throws IllegalArgumentException, IllegalAccessException,
			NoSuchFieldException {
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
	public ArrayList<PNEvent> queryEventsFromCalendar(Context context) {
		ArrayList<PNEvent> events = new ArrayList<PNEvent>();
		//CalendarContract.Events.CALENDAR_ID != 1 表示只查询未被删除的事件
		Cursor cur = context.getContentResolver().query(
				Events.CONTENT_URI,
				new String[] { "_id", "title", "dtstart", "dtend","description" },
				CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
						+ CalendarContract.Events.DELETED + " != ?",
				new String[] { String.valueOf(this._id), "1" }, "dtstart ASC");
		
//		String[] l = cur.getColumnNames();
		
		while (cur.moveToNext()) {
			PNEvent pnEvent = new PNEvent();
			pnEvent.calendar_id = this._id;
			pnEvent.title = cur.getString(cur.getColumnIndex("title"));
			pnEvent.dtstart = cur.getLong(cur.getColumnIndex("dtstart"));
			pnEvent.dtend = cur.getLong(cur.getColumnIndex("dtend"));
			pnEvent._id = cur.getLong(cur.getColumnIndex("_id"));
			pnEvent.description = cur.getString(cur.getColumnIndex("description"));
			events.add(pnEvent);
		}
		cur.close();
		return events;
	}
	
	@SuppressLint("NewApi")
	public long insertEvent(Context context, long cal_id, String timezone, 
			String title, String description, String place, 
			Calendar start, Calendar end, 
			String hasAlarm) {
		try{
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			//	values
			values.put(CalendarContract.Events.CALENDAR_ID, cal_id);
			values.put(CalendarContract.Events.TITLE, title);
			values.put(CalendarContract.Events.DESCRIPTION, description);
			values.put(CalendarContract.Events.EVENT_LOCATION, place);
			values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
			values.put(CalendarContract.Events.DTSTART, start.getTimeInMillis());
			values.put(CalendarContract.Events.DTEND, end.getTimeInMillis());
			values.put(CalendarContract.Events.ALL_DAY, "0");	
			values.put(CalendarContract.Events.HAS_ALARM, hasAlarm);
			//	Uri,long_id
			Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
			long id = Long.parseLong(uri.getLastPathSegment());

			return id;
		}catch(Exception e){
			return -1;
		}
	}
	
	@SuppressLint("NewApi")
	public long insertEvent(Context context, long cal_id, String timezone, 
			String title, String description, String place, 
			Calendar date) {
		try{
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			//	values
			values.put(CalendarContract.Events.CALENDAR_ID, cal_id);
			values.put(CalendarContract.Events.TITLE, title);
			values.put(CalendarContract.Events.DESCRIPTION, description);
			values.put(CalendarContract.Events.EVENT_LOCATION, place);
			values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
			values.put(CalendarContract.Events.DTSTART, date.getTimeInMillis());
			values.put(CalendarContract.Events.DTEND, date.getTimeInMillis());
			//	Uri,long_id
			Uri uri = cr.insert(Events.CONTENT_URI, values);
			long id = Long.parseLong(uri.getLastPathSegment());

			return id;
		}catch(Exception e){
			return -1;
		}
	}
	
	@SuppressLint("NewApi")
	public boolean deleteEvent(Context context,long event_id) {
		try{
			ContentResolver cr = context.getContentResolver();
			//	Uri,delete
			Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, event_id);
			int rows = cr.delete(uri, null, null);
			if(rows>0){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	public boolean updateEvent(Context context,long event_id, long cal_id, String timezone, 
			String title, String description, String place, 
			Calendar start, Calendar end, 
			String hasAlarm, String allDay) {
		try{
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			//	values
			if(cal_id!=-1)	values.put(Events.CALENDAR_ID, cal_id);
			if(title!=null)	values.put(Events.TITLE, title);
			if(description!=null)	values.put(Events.DESCRIPTION, description);
			if(place!=null)	values.put(Events.EVENT_LOCATION, place);
			if(timezone!=null)	values.put(Events.EVENT_TIMEZONE, timezone);
			if(start!=null)	values.put(Events.DTSTART, start.getTimeInMillis());
			if(end!=null)	values.put(Events.DTEND, end.getTimeInMillis());
			if(allDay!=null)	values.put(Events.ALL_DAY, allDay);
			if(hasAlarm!=null)	values.put(Events.HAS_ALARM, hasAlarm);
			//	Uri,long_id
			String where = Events._ID + "=?";
			String[] selectionArgs = new String[]{
				String.valueOf(event_id)
			};
			int rows = cr.update(Events.CONTENT_URI, values, where, selectionArgs);
			if(rows>0){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			return false;
		}
	}
}
