package com.pnwedding.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.timessquare.sample.R;

public class CheckImageView extends ImageView {
	
	public boolean checked;
	
	 public CheckImageView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         setImageResource(R.drawable.checkbox_off_background);
     }

     public CheckImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
         setImageResource(R.drawable.checkbox_off_background);
     }

     public CheckImageView(Context context) {
         super(context);
         setImageResource(R.drawable.checkbox_off_background);
     }

 
	
	public void refreshImage(){
		if (checked) {
			setImageResource(R.drawable.checkbox_on_background);
		}else {
			setImageResource(R.drawable.checkbox_off_background);
		}
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		refreshImage();
	}
	

	
}
