package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.Logs;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.edmondapps.utils.android.annotaion.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.photos.ui.fragment.PhotoSelectFragment;

import java.util.ArrayList;

@ParentActivity(PhotosActivity.class)
public class PhotosSelectActivity extends SinglePaneActivity implements PhotoSelectFragment.Callback {
    public static final String TAG = "PhotosSelectActivity";
    public static final String KEY_FILES_PATHS = "key_files_paths";

    public static void start(Context context) {
        context.startActivity(PhotosSelectActivity.of(context));
    }

    public static Intent of(Context context) {
        return new Intent(context, PhotosSelectActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        setResult(RESULT_CANCELED);
        super.onCreate(savedState);
    }

    @Override
    protected int onCreateLayoutId() {
        return R.layout.no_padding_activity;
    }

    @Override
    protected Fragment onCreateFragment() {
        return new PhotoSelectFragment();
    }

    @Override
    public void onCancel(PhotoSelectFragment f) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onOk(PhotoSelectFragment f, ArrayList<String> paths) {
        Logs.d(TAG, paths.toString());
        final Intent data = new Intent().putExtra(KEY_FILES_PATHS, paths);
        setResult(RESULT_OK, data);
        finish();
    }
}
