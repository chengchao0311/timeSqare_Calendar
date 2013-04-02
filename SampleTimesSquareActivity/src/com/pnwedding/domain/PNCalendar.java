package com.pnwedding.domain;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
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
import android.provider.CalendarContract.Reminders;
import android.provider.ContactsContract;
import android.util.Log;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.MonthCellDescriptor;
import com.squareup.timessquare.sample.CalendarPage;
import com.squareup.timessquare.sample.ToDoAdapter;

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

	// *********************************************//
	// ******************日历操作方法******************//
	// *********************************************//
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
	public ArrayList<PNEvent> queryCalendaerEventsRefreshCPV(
			final Context context, final ArrayList<PNEvent> events,
			final CalendarPickerView calendarPickerView) {
		events.clear();// 先清空之前的事件
		// CalendarContract.Events.CALENDAR_ID != 1 表示只查询未被删除的事件
		ContentResolver cr = context.getContentResolver();
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(
				context.getContentResolver()) {
			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				while (cursor.moveToNext()) {
					PNEvent pnEvent = new PNEvent();
					pnEvent.calendar_id = PNCalendar.this._id;
					pnEvent.title = cursor.getString(cursor
							.getColumnIndex("title"));
					pnEvent.dtstart = cursor.getLong(cursor
							.getColumnIndex("dtstart"));
					pnEvent.dtend = cursor.getLong(cursor
							.getColumnIndex("dtend"));
					pnEvent._id = cursor.getLong(cursor.getColumnIndex("_id"));
					pnEvent.description = cursor.getString(cursor
							.getColumnIndex("description"));

					String hasAVal = cursor.getString(cursor
							.getColumnIndex("hasAlarm"));
					if (hasAVal.equalsIgnoreCase("0")) {
						pnEvent.hasAlarm = false;
					} else {
						pnEvent.hasAlarm = true;
					}

					events.add(pnEvent);
				}
				cursor.close();
				// 刷新 CalendarPV
				calendarPickerView.getAdapter().notifyDataSetChanged();
			}
		};

		queryHandler.startQuery(111, new Object(), Events.CONTENT_URI,
				new String[] { "_id", "title", "dtstart", "dtend",
						"description", "hasAlarm" },
				CalendarContract.Events.CALENDAR_ID + " = ?" + " AND "
						+ CalendarContract.Events.DELETED + " != ?",
				new String[] { String.valueOf(PNCalendar.this._id), "1" },
				"dtstart ASC");

		return events;
	}

	@SuppressLint({ "NewApi", "InlinedApi" })
	public void queryAndFillOneDayEventAndRefreshToDoList(Context context,
			long timeDuringTheDay, final ArrayList<PNEvent> oneDayEvents,
			final ToDoAdapter toDoAdapter) {
		oneDayEvents.clear();
		long dayStartMill = getDayStartMill(timeDuringTheDay);
		long dayEndMill = getDayEndMill(timeDuringTheDay);

		ContentResolver cr = context.getContentResolver();
		AsyncQueryHandler queryHandler = new AsyncQueryHandler(cr) {
			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				while (cursor.moveToNext()) {
					PNEvent pnEvent = new PNEvent();
					pnEvent.calendar_id = PNCalendar.this._id;
					pnEvent.title = cursor.getString(cursor
							.getColumnIndex("title"));
					pnEvent.dtstart = cursor.getLong(cursor
							.getColumnIndex("dtstart"));
					pnEvent.dtend = cursor.getLong(cursor
							.getColumnIndex("dtend"));
					pnEvent._id = cursor.getLong(cursor.getColumnIndex("_id"));
					pnEvent.description = cursor.getString(cursor//
							.getColumnIndex("description"));
					String hasAVal = cursor.getString(cursor//
							.getColumnIndex("hasAlarm"));
					if (hasAVal.equalsIgnoreCase("0")) {
						pnEvent.hasAlarm = false;
					} else {
						pnEvent.hasAlarm = true;
					}
					oneDayEvents.add(pnEvent);
				}
				cursor.close();
				toDoAdapter.notifyDataSetChanged();
			}
		};

		queryHandler.startQuery(112, new Object(), Events.CONTENT_URI,
				new String[] { "_id", "title", "dtstart", "dtend",
						"description", "hasAlarm" }, //
				CalendarContract.Events.CALENDAR_ID + " = ?" //
						+ " AND " + //
						CalendarContract.Events.DELETED + " != ?"//
						+ " AND " + //
						CalendarContract.Events.DTSTART + " >= ?" + " AND " + //
						CalendarContract.Events.DTEND + " <= ?"//
				, new String[] { String.valueOf(PNCalendar.this._id), "1",
						dayStartMill + "", dayEndMill + "" }, "dtstart ASC");

	}

	@SuppressLint("NewApi")
	public long insertEvent(Context context, String title, String description,
			Calendar start, Calendar end, Long alarmTime) {
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
			if (alarmTime == 0L) {
				values.put(CalendarContract.Events.HAS_ALARM, "0");
			} else {
				values.put(CalendarContract.Events.HAS_ALARM, "1");
			}
			// Uri,long_id
			Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
			long id = Long.parseLong(uri.getLastPathSegment());

			if (alarmTime != 0L) {// add reminder for the event
				addReminder(context, id, alarmTime);
			}

			return id;
		} catch (Exception e) {
			return -1;
		}
	}

	@SuppressLint("NewApi")
	public boolean deleteEvent(Context context, long event_id) {
		try {
			ContentResolver cr = context.getContentResolver();
			deleteReminderOfTheEvent(event_id, cr);

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

	/**
	 * @param context
	 * @param event_id
	 * @param cal_id
	 * @param title
	 * @param description
	 * @param start
	 * @param end
	 * @param hasAlarm
	 * @param allDay
	 * @return
	 */
	@SuppressLint({ "InlinedApi", "NewApi" })
	public boolean updateEvent(Context context, long event_id, long cal_id,
			String title, String description,
			Calendar start, Calendar end, long hasAlarm) {
		try {
			ContentResolver cr = context.getContentResolver();
			ContentValues values = new ContentValues();
			// values
			if (cal_id != -1){
				values.put(Events.CALENDAR_ID, cal_id);
			}
			if (title != null){
				values.put(Events.TITLE, title);
			}
			if (description != null){
				values.put(Events.DESCRIPTION, description);
			}
			if (start != null){
				values.put(Events.DTSTART, start.getTimeInMillis());
			}
			if (end != null){
				values.put(Events.DTEND, end.getTimeInMillis());
			}
			values.put(Events.ALL_DAY, "0");
			if (hasAlarm != 0L) {//事件部分 
				values.put(Events.HAS_ALARM, "1");
			} else {
				values.put(Events.HAS_ALARM, "0");
			}
			
			updateReminder(context, event_id, hasAlarm, cr);


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

	/**
	 * @param context
	 * @param event_id
	 * @param hasAlarm
	 * @param cr
	 */
	public void updateReminder(Context context, long event_id, long hasAlarm,
			ContentResolver cr) {
		long minute = queryReminder(context, event_id);// 查询之前的提醒时间
		if (minute != 0) {// 没提醒的话会返回0, 如果之前有
			if (hasAlarm != 0) {//
				if (minute == hasAlarm) {// 有,但是没有改动时间
					// 什么都不做
				} else {// 有但是改动过时间
					deleteReminderOfTheEvent(event_id, cr);
					addReminder(context, event_id, hasAlarm);
				}
			} else {// 如果是从有改成没有,直接删除之前的
				deleteReminderOfTheEvent(event_id, cr);
			}
		} else {// 如果之前没有
			if (hasAlarm != 0) {//没有改成有
				addReminder(context, event_id, hasAlarm);
			}else {//都没有
				//什么都不做
			}
		}
	}

	@SuppressLint("NewApi")
	public void addReminder(Context context, long event_id, long minute) {
		ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(Reminders.MINUTES, minute);
		values.put(Reminders.EVENT_ID, event_id);
		values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
		cr.insert(Reminders.CONTENT_URI, values);
	}

	@SuppressLint("NewApi")
	public long queryReminder(Context context, long event_id) {
		ContentResolver cr = context.getContentResolver();
		Cursor cur = cr.query(Reminders.CONTENT_URI,
				new String[] { "minutes" }, Reminders.EVENT_ID + " = ?",
				new String[] { "" + event_id }, null);
		long minute = 0;
		while (cur.moveToNext()) {
			minute = cur.getLong(0);
		}

		return minute;
	}

	/**
	 * @param event_id
	 * @param cr
	 */
	@SuppressLint("NewApi")
	public void deleteReminderOfTheEvent(long event_id, ContentResolver cr) {
		Cursor cur = Reminders.query(cr, event_id, null);
		while (cur.moveToNext()) {
			Uri reminderuri = ContentUris.withAppendedId(Reminders.CONTENT_URI,
					cur.getLong(cur.getColumnIndex("_id")));
			cr.delete(reminderuri, null, null);
		}
	}

	// ***************************************//
	// ******************工具方法***************//
	// ***************************************//

	public static long getDayStartMill(long time) {
		Calendar tmpCal = Calendar.getInstance();
		tmpCal.setTimeInMillis(time);
		tmpCal.set(Calendar.HOUR_OF_DAY, 0);
		tmpCal.set(Calendar.MINUTE, 0);
		tmpCal.set(Calendar.SECOND, 0);
		tmpCal.set(Calendar.MILLISECOND, 0);
		return tmpCal.getTimeInMillis();
	}

	public static long getDayEndMill(long time) {
		Calendar tmpCal = Calendar.getInstance();
		tmpCal.setTimeInMillis(time);
		tmpCal.set(Calendar.HOUR_OF_DAY, 23);
		tmpCal.set(Calendar.MINUTE, 11);
		tmpCal.set(Calendar.SECOND, 59);
		tmpCal.set(Calendar.MILLISECOND, 59);
		return tmpCal.getTimeInMillis();
	}

	// ***************************************//
	// ******************作廢方法***************//
	// ***************************************//

	// @SuppressLint({ "InlinedApi", "NewApi" })
	// public ArrayList<PNEvent> queryOneDayEventsFromCalendar(Context context,
	// final ArrayList<PNEvent> oneDayEvents, long time,
	// final Handler handler, final ToDoAdapter toDoAdapter) {
	// oneDayEvents.clear();
	// long dayStartMill = getDayStartMill(time);
	// long dayEndMill = getDayEndMill(time);
	//
	// ContentResolver cr = context.getContentResolver();
	// AsyncQueryHandler queryHandler = new AsyncQueryHandler(cr) {
	// @Override
	// protected void onQueryComplete(int token, Object cookie,
	// Cursor cursor) {
	// while (cursor.moveToNext()) {
	// PNEvent pnEvent = new PNEvent();
	// pnEvent.calendar_id = PNCalendar.this._id;
	// pnEvent.title = cursor.getString(cursor
	// .getColumnIndex("title"));
	// pnEvent.dtstart = cursor.getLong(cursor
	// .getColumnIndex("dtstart"));
	// pnEvent.dtend = cursor.getLong(cursor
	// .getColumnIndex("dtend"));
	// pnEvent._id = cursor.getLong(cursor.getColumnIndex("_id"));
	// pnEvent.description = cursor.getString(cursor
	// .getColumnIndex("description"));
	// oneDayEvents.add(pnEvent);
	// }
	// toDoAdapter.notifyDataSetChanged();
	// }
	// };
	//
	// queryHandler.startQuery(111, new Object(), Events.CONTENT_URI,
	// new String[] { "_id", "title", "dtstart", "dtend",
	// "description" }, CalendarContract.Events.CALENDAR_ID
	// + " = ?" + " AND "
	// + CalendarContract.Events.DELETED//
	// + " != ?" + " AND " + CalendarContract.Events.DTSTART
	// + " >= ?" //
	// + " AND " + CalendarContract.Events.DTEND + " <= ?",
	// new String[] { String.valueOf(PNCalendar.this._id), "1",
	// dayStartMill + "", dayEndMill + "" }, "dtstart ASC");
	//
	// return oneDayEvents;
	// }

	//
	// public static void fillOneDayEvents(long timeOfTheDay,
	// ArrayList<PNEvent> events, ArrayList<PNEvent> onDayEvents) {
	// long dayStartMill = getDayStartMill(timeOfTheDay);
	// long dayEndMill = getDayEndMill(timeOfTheDay);
	// for (int i = 0; i < events.size(); i++) {
	// PNEvent aEvent = events.get(i);
	// if (aEvent.dtend < dayStartMill || aEvent.dtstart > dayEndMill) {
	// continue;// 結束時間小於本日開始時間或者開始時間大於本日借宿時間，本日不在事件區間內
	// } else {
	// onDayEvents.add(aEvent);
	// }
	// }
	// }
}
