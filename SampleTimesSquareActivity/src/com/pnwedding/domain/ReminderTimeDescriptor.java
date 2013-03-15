package com.pnwedding.domain;

public class ReminderTimeDescriptor {
	public String text;
	public long timeMills;

	public ReminderTimeDescriptor setText(String text) {
		this.text = text;
		return this;
	}

	public ReminderTimeDescriptor setTimeMills(Long timeMills) {
		this.timeMills = timeMills;
		return this;
	}
}
