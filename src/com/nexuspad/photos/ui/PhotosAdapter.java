package com.nexuspad.photos.ui;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nexuspad.R;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.datamodel.Photo;
import com.nexuspad.dataservice.EntryListService;
import com.nexuspad.ui.EntriesAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PhotosAdapter extends EntriesAdapter<Photo> {

    private final Activity mActivity;
    private final Picasso mPicasso;

    /**
     * use this constructor if you want filtering abilities
     *
     * @param a
     * @param entries
     * @param folder
     * @param service
     * @param template
     */
    public PhotosAdapter(Activity a, List<Photo> entries, Folder folder, EntryListService service, EntryTemplate template) {
        super(a, entries, folder, service, template);
        mActivity = a;
        mPicasso = Picasso.with(a);
    }

    @Override
    protected View getEntryView(Photo entry, int position, View convertView, ViewGroup parent) {
        final ImageView view;

        if (convertView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            view = (ImageView) inflater.inflate(R.layout.layout_photo_grid, parent, false);
        } else {
            view = (ImageView) convertView;
        }

        mPicasso.load(getItem(position).getTnUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_launcher)
                .resizeDimen(R.dimen.photo_grid_width, R.dimen.photo_grid_height)
                .centerCrop()
                .into(view);

        return view;
    }

    @Override
    protected View getEmptyEntryView(LayoutInflater i, View c, ViewGroup p) {
        return getCaptionView(i, c, p, R.string.empty_photos, R.drawable.empty_folder);
    }

    @Override
    protected String getEntriesHeaderText() {
        return null;
    }
}