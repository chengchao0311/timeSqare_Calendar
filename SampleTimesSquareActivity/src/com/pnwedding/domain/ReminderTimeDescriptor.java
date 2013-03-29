package com.pnwedding.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class ReminderTimeDescriptor implements Parcelable{
	public String text;
	public long timeMills;

	public void setText(String text) {
		this.text = text;
	}

	public void setTimeMills(Long timeMills) {
		this.timeMills = timeMills;
	}

	
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(text);
		dest.writeLong(timeMills);
	}
	
	public static final Parcelable.Creator<ReminderTimeDescriptor> CREATOR = new Creator<ReminderTimeDescriptor>(
			) {

				@Override
				public ReminderTimeDescriptor createFromParcel(Parcel source) {
					ReminderTimeDescriptor rtd = new ReminderTimeDescriptor();
					rtd.setText(source.readString());
					rtd.setTimeMills(source.readLong());
					
					return rtd;
				}

				@Override
				public ReminderTimeDescriptor[] newArray(int size) {
					return new ReminderTimeDescriptor[size];
				}
	};
}
