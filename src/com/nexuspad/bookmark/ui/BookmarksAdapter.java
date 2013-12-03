/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.bookmark.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nexuspad.R;
import com.nexuspad.datamodel.Bookmark;
import com.nexuspad.ui.EntriesAdapter;

import java.util.List;

/**
 * @author Edmond
 */
public class BookmarksAdapter extends EntriesAdapter<Bookmark> {

    public BookmarksAdapter(Activity a, List<? extends Bookmark> entries) {
        super(a, entries);
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
}
