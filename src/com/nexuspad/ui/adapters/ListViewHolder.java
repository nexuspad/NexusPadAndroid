package com.nexuspad.ui.adapters;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A generic view holder for list that has both entries and folders.
 *
 * Created by ren on 7/18/14.
 */
public class ListViewHolder {
	ImageView icon;
	ImageButton icon2;
	TextView text1;
	ImageButton menu;

	public ImageView getIcon() {
		return icon;
	}

	public TextView getText1() {
		return text1;
	}

	public void setText1(TextView textView) {
		text1 = textView;
	}

	public ImageButton getMenu() {
		return menu;
	}
}
