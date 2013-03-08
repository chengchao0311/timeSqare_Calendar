package com.squareup.timessquare;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pnwedding.domain.PNEvent;
import com.squareup.timessquare.sample.R;

public class ToDoAdapter extends ArrayAdapter<PNEvent> {
	public Context context;
	public int resource;
	public int textViewResourceId;
	public ArrayList<PNEvent> list;
	
	public ToDoAdapter(Context context, int resource, int textViewResourceId,
			List<PNEvent> list) {
		super(context, resource, textViewResourceId, list);
		this.context = context;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
		this.list = (ArrayList<PNEvent>)list;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = ((Activity)context).getLayoutInflater().inflate(R.layout.to_do_list,parent,false);
		TextView textView = (TextView) view.findViewById(R.id.to_do_item);
		textView.setText(list.get(position).title);
		return view;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}
}
