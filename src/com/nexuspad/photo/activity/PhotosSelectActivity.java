package com.nexuspad.photo.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.nexuspad.common.activity.SinglePaneActivity;
import com.nexuspad.common.annotation.ParentActivity;
import com.nexuspad.R;
import com.nexuspad.photo.fragment.PhotoSelectFragment;
import com.nexuspad.service.util.Logs;

import java.util.ArrayList;

@ParentActivity(PhotosActivity.class)
public class PhotosSelectActivity extends SinglePaneActivity implements PhotoSelectFragment.Callback {
    public static final String TAG = "PhotosSelectActivity";

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
        return R.layout.np_padding_activity;
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
    public void onOK(PhotoSelectFragment f, ArrayList<String> paths) {
        Logs.d(TAG, paths.toString());

        final Intent data = new Intent().putParcelableArrayListExtra(Intent.EXTRA_STREAM, fromString(paths));
        setResult(RESULT_OK, data);
        finish();
    }

    private static ArrayList<Uri> fromString(Iterable<String> paths) {
        final ArrayList<Uri> uris = new ArrayList<Uri>();
        for (String path : paths) {
            uris.add(Uri.parse(path));
        }
        return uris;
    }
}
