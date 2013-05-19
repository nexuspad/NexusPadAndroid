/*
 * Copyright (C), NexusPad LLC
 */
package com.nexuspad.doc.ui;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nexuspad.R;
import com.nexuspad.datamodel.Doc;
import com.nexuspad.ui.EntriesAdapter;

/**
 * @author Edmond
 * 
 */
public class DocsAdapter extends EntriesAdapter<Doc> {

    public DocsAdapter(Activity a, List<? extends Doc> entries) {
        super(a, entries);
    }

    @Override
    protected View getEntryView(Doc entry, int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.list_item_icon, parent, false);
        }
        ViewHolder holder = getHolder(convertView);

        holder.icon.setImageResource(R.drawable.ic_doc);
        holder.text1.setText(entry.getTitle());
        holder.menu.setOnClickListener(getOnMenuClickListener());

        return convertView;
    }

    @Override
    protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
        return getCaptionView(i, c, p, R.string.empty_docs, R.drawable.empty_folder);
    }

    @Override
    protected int getEntryStringId() {
        return R.string.docs;
    }
}
