package com.squareup.timessquare.sample;

import java.util.ArrayList;

import com.pnwedding.domain.PNEvent;

public interface ToDoListCallBack {
	public void showEventsForTheDay(ArrayList<PNEvent> oneDayEventsFromCell);;
}
