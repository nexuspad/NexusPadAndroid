/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.common.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.nexuspad.R;

/**
 * @author Edmond
 */
public class IconListAdapter extends BaseAdapter {

    private final int[] mDrawableIds;
    private final CharSequence[] mStrings;
    private final LayoutInflater mLayoutInflater;

    private boolean mHasMenu;

    public IconListAdapter(Activity activity, int[] drawableIds, int[] stringIds) {
        this(activity, drawableIds, toStrings(activity, stringIds));
    }

    public IconListAdapter(Activity activity, int[] drawableIds, CharSequence[] strings) {
        if (drawableIds.length != strings.length) {
            throw new IllegalArgumentException("drawableIds and stringIds must have the same length");
        }
        mDrawableIds = drawableIds;
        mStrings = strings;
        mLayoutInflater = activity.getLayoutInflater();
    }

    private static CharSequence[] toStrings(Context c, int[] stringIds) {
        CharSequence[] out = new CharSequence[stringIds.length];
        int i = 0;
        for (int id : stringIds) {
            out[i++] = c.getText(id);
        }
        return out;
    }

    public final boolean hasMenu() {
        return mHasMenu;
    }

    public final void setHasMenu(boolean hasMenu) {
        if (mHasMenu != hasMenu) {
            mHasMenu = hasMenu;
            notifyDataSetChanged();
        }
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
            holder.icon = (ImageView)convertView.findViewById(android.R.id.icon);
            holder.text1 = (TextView)convertView.findViewById(android.R.id.text1);
            holder.menu = (ImageButton)convertView.findViewById(R.id.menu);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.icon.setImageResource(mDrawableIds[position]);
        holder.text1.setText(mStrings[position]);

        holder.menu.setVisibility(mHasMenu ? View.VISIBLE : View.GONE);

        return convertView;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text1;
        ImageButton menu;
    }
}
