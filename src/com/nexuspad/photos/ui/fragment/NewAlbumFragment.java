package com.nexuspad.photos.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.edmondapps.utils.android.annotaion.FragmentName;
import com.nexuspad.R;
import com.nexuspad.annotation.ModuleId;
import com.nexuspad.datamodel.Album;
import com.nexuspad.datamodel.EntryTemplate;
import com.nexuspad.datamodel.Folder;
import com.nexuspad.dataservice.ServiceConstants;
import com.nexuspad.ui.fragment.NewEntryFragment;

import static com.edmondapps.utils.android.view.ViewUtils.findView;

@FragmentName(NewAlbumFragment.TAG)
@ModuleId(moduleId = ServiceConstants.PHOTO_MODULE, template = EntryTemplate.ALBUM)
public class NewAlbumFragment extends NewEntryFragment<Album> {
    public static final String TAG = "NewAlbumFragment";

    private static final int REQ_PICK_IMAGES = 1;

    public static NewAlbumFragment of(Folder folder) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_FOLDER, folder);

        final NewAlbumFragment fragment = new NewAlbumFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    private View mAddMoreV;
    private TextView mNumPhotosV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_new_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAddMoreV = findView(view, R.id.pick_img);
        mNumPhotosV = findView(view, R.id.lbl_num_photos);
        mNumPhotosV.setText(getResources().getQuantityString(R.plurals.numberOfPhotos, 0, 0));
//        mAddMoreV.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final FragmentActivity activity = getActivity();
//                pick images
//                startActivityForResult(intent, REQ_PICK_IMAGES);
//            }
//        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_PICK_IMAGES:
                if (resultCode == Activity.RESULT_OK) {
//                    do work
                }
                break;
            default:
                throw new AssertionError("unknown requestCode: " + requestCode);
        }
    }

    @Override
    public boolean isEditedEntryValid() {
        return false;
    }

    @Override
    public Album getEditedEntry() {
        return null;
    }
}
