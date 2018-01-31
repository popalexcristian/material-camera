package com.afollestad.materialcamera.internal;

import android.app.Activity;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.afollestad.materialcamera.R;
import com.afollestad.materialdialogs.MaterialDialog;

public abstract class BaseGalleryFragment extends Fragment
        implements CameraUriInterface, View.OnClickListener {

    BaseCaptureInterface mInterface;
    int mPrimaryColor;
    String mOutputUri;
    View mControlsFrame;
    ImageView mRetry;
    ImageView mConfirm;
    ImageView mCrop;
    View mWhiteView;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mInterface = (BaseCaptureInterface) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOutputUri = getArguments().getString("output_uri");
        mControlsFrame = view.findViewById(R.id.controlsFrame);
        mRetry = (ImageView) view.findViewById(R.id.retry);
        mConfirm = (ImageView) view.findViewById(R.id.confirm);
        mCrop = (ImageView) view.findViewById(R.id.crop);
        mWhiteView = (View) view.findViewById(R.id.white_view);
        mCrop.setVisibility(View.GONE);

        mRetry.setVisibility(
                getArguments().getBoolean(CameraIntentKey.ALLOW_RETRY, true) ? View.VISIBLE : View.GONE);
    }

    @Override
    public String getOutputUri() {
        return getArguments().getString("output_uri");
    }

    void showDialog(String title, String errorMsg) {
        new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(errorMsg)
                .positiveText(android.R.string.ok)
                .show();
    }
}
