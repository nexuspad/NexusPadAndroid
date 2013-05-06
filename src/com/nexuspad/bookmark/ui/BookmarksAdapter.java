/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.ui.EntriesAdapter;

/**
 * @author Edmond
 * 
 */
public class BookmarksAdapter extends EntriesAdapter<Bookmark> {

    public BookmarksAdapter(Activity a, List<? extends Bookmark> entries) {
        super(a, entries);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder holder = getHolder(view);
        OnClickListener listener = getOnMenuClickListener();
        if (listener != null) {
            listener.onClick(holder.menu);
        }
        return true;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView text1;
        ImageButton menu;
    }

    private static ViewHolder getHolder(View convertView) {
        ViewHolder holder = (ViewHolder)convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.icon = findView(convertView, android.R.id.icon);
            holder.text1 = findView(convertView, android.R.id.text1);
            holder.menu = findView(convertView, R.id.menu);
            holder.menu.setFocusable(false);

            convertView.setTag(holder);
        }
        return holder;
    }

    @Override
    protected View getEntryView(Bookmark entry, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
        }
        ViewHolder holder = getHolder(convertView);

        holder.icon.setImageResource(R.drawable.ic_bookmark);
        holder.text1.setText(entry.getTitle());
        holder.menu.setOnClickListener(getOnMenuClickListener());

        return convertView;
    }

    @Override
    protected int getEntryStringId() {
        return R.string.bookmarks;
    }

    @Override
    protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
        return getCaptionView(i, c, p, R.string.empty_bookmarks, R.drawable.empty_folder);
    }

    private static View getCaptionView(LayoutInflater i, View c, ViewGroup p, int stringId, int drawableId) {
        ViewHolder holder;
        if (c == null) {
            c = i.inflate(R.layout.layout_img_caption, p, false);

            holder = new ViewHolder();
            holder.text1 = findView(c, android.R.id.text1);

            c.setTag(holder);
        } else {
            holder = (ViewHolder)c.getTag();
        }
        holder.text1.setText(stringId);
        holder.text1.setCompoundDrawablesWithIntrinsicBounds(0, drawableId, 0, 0);
        return c;
    }
}
