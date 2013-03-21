package com.pnwedding.domain;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.CalendarContract;


public class PNEvent implements Parcelable{
	
	public long _id;
	public long	dtstart;
	public long	dtend;
    public long	calendar_id	;
    public boolean hasAlarm;
    public String title;
    public String description;
    
    
    
    public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public long getDtstart() {
		return dtstart;
	}

	public void setDtstart(long dtstart) {
		this.dtstart = dtstart;
	}

	public long getDtend() {
		return dtend;
	}

	public void setDtend(long dtend) {
		this.dtend = dtend;
	}

	public long getCalendar_id() {
		return calendar_id;
	}

	public void setCalendar_id(long calendar_id) {
		this.calendar_id = calendar_id;
	}

	public boolean isHasAlarm() {
		return hasAlarm;
	}

	public void setHasAlarm(boolean hasAlarm) {
		this.hasAlarm = hasAlarm;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static Parcelable.Creator<PNEvent> getCreator() {
		return CREATOR;
	}
	
	
	
	@Override
	public boolean equals(Object o) {
		
		return this._id == ((PNEvent)o)._id;
	}
	
	
	//------------------------------------------------------------------------
	@SuppressLint("NewApi")
	public PNReminder queryPNReminder(Context context){//本app 只返回一個Reminder
    	PNReminder pnReminder = new PNReminder();
    	pnReminder.event_id = _id;
    	ContentResolver cr = context.getContentResolver();
    	Cursor cur = CalendarContract.Reminders.query(cr, _id, new String[] {"minutes"});
    	while (cur.moveToNext()) {
    		pnReminder.minutes = cur.getLong(cur.getColumnIndex("minutes"));
		}
    	cur.close();
    	return pnReminder;
    }

	//------------------------------------------------------------------------
	@Override
	public int describeContents() {
	
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(_id);
		dest.writeLong(dtstart);
		dest.writeLong(dtend);
		dest.writeLong(calendar_id);
		dest.writeByte((byte)(hasAlarm ? 1:0));
		dest.writeString(title);
		dest.writeString(description);
	}
	
    
		
	public static final Parcelable.Creator<PNEvent> CREATOR = new Parcelable.Creator<PNEvent>() {

		@Override
		public PNEvent createFromParcel(Parcel source) {
			PNEvent pnEvent = new PNEvent();
			pnEvent.set_id(source.readLong());
			pnEvent.setDtstart(source.readLong());
			pnEvent.setDtend(source.readLong());
			pnEvent.setCalendar_id(source.readLong());
			pnEvent.setHasAlarm(source.readByte() == 1);
			pnEvent.setTitle(source.readString());
			pnEvent.setDescription(source.readString());
			return pnEvent;
		}

		@Override
		public PNEvent[] newArray(int size) {
			return new PNEvent[size];
		}
	};
    
}
