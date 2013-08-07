package com.nexuspad.photos.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.edmondapps.utils.android.activity.SinglePaneActivity;
import com.nexuspad.photos.ui.fragment.PhotoSelectFragment;

import java.util.ArrayList;
import java.util.List;

public class PhotosSelectActivity extends SinglePaneActivity implements PhotoSelectFragment.Callback {

    public static void start(Context context) {
        final Intent intent = new Intent(context, PhotosSelectActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedState) {
        setResult(RESULT_CANCELED);
        super.onCreate(savedState);
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
        new Intent().putExtra("", paths);
        setResult(RESULT_OK);
        finish();
    }
}
