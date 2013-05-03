/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.ui;

import static com.edmondapps.utils.android.view.ViewUtils.findView;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexuspad.R;

/**
 * @author Edmond
 * 
 */
public class IconListAdapter extends BaseAdapter {

    private final int[] mDrawableIds;
    private final int[] mStringIds;
    private final LayoutInflater mLayoutInflater;

    public IconListAdapter(Activity activity, int[] drawableIds, int[] stringIds) {
        if (drawableIds.length != stringIds.length) {
            throw new IllegalArgumentException("drawableIds and stringIds must have the same length");
        }
        mDrawableIds = drawableIds;
        mStringIds = stringIds;
        mLayoutInflater = activity.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return mDrawableIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item_icon, parent, false);
            holder = new ViewHolder();
            holder.icon = findView(convertView, android.R.id.icon);
            holder.text1 = findView(convertView, android.R.id.text1);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.icon.setImageResource(mDrawableIds[position]);
        holder.text1.setText(mStringIds[position]);

        return convertView;
    }

    private class ViewHolder {
        ImageView icon;
        TextView text1;
    }
}
