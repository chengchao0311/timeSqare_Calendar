package com.squareup.timessquare;

import java.util.ArrayList;

import com.pnwedding.domain.PNEvent;

public interface ToDoListCallBack {
	public void showEventsOfTheDay(ArrayList<PNEvent> oneDayEventsFromCell);;
}