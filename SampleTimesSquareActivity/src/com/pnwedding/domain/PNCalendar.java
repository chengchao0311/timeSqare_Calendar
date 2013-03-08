package com.pnwedding.domain;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
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
				new String[] { "_id", "title", "dtstart", "dtend" },
				CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
						+ CalendarContract.Events.DELETED + " != ?",
				new String[] { String.valueOf(this._id), "1" }, "dtstart ASC");
		
		String[] l = cur.getColumnNames();
		
		while (cur.moveToNext()) {
			PNEvent pnEvent = new PNEvent();
			pnEvent.calendar_id = this._id;
			pnEvent.title = cur.getString(cur.getColumnIndex("title"));
			pnEvent.dtstart = cur.getLong(cur.getColumnIndex("dtstart"));
			pnEvent.dtend = cur.getLong(cur.getColumnIndex("dtend"));
			pnEvent._id = cur.getLong(cur.getColumnIndex("_id"));
			events.add(pnEvent);
		}
		cur.close();
		return events;
	}
}
