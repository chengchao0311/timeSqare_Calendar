package com.pnwedding.utils;

import java.util.Comparator;

import com.pnwedding.domain.ReminderTimeDescriptor;

public class ReminderTimeDescriptorComPara implements Comparator<ReminderTimeDescriptor> {

	@Override
	public int compare(ReminderTimeDescriptor rtd0, ReminderTimeDescriptor rtd1) {
		return (int) (rtd0.timeMills - rtd1.timeMills);
	}

}
