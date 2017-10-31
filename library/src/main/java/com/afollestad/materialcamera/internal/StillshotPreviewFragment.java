package com.afollestad.materialcamera.internal;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.afollestad.materialcamera.R;
import com.bumptech.glide.Glide;

import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_FRONT;

public class StillshotPreviewFragment extends BaseGalleryFragment {

    private ImageView mImageView;

    /**
     * Reference to the bitmap, in case 'onConfigurationChange' event comes, so we do not recreate the
     * bitmap
     */
    public static StillshotPreviewFragment newInstance(
            String outputUri, boolean allowRetry, int primaryColor) {
        final StillshotPreviewFragment fragment = new StillshotPreviewFragment();
        fragment.setRetainInstance(true);
        Bundle args = new Bundle();
        args.putString("output_uri", outputUri);
        args.putBoolean(CameraIntentKey.ALLOW_RETRY, allowRetry);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mcam_fragment_stillshot, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageView = (ImageView) view.findViewById(R.id.stillshot_imageview);

        mRetry.setOnClickListener(this);
        mConfirm.setOnClickListener(this);

        if (getActivity() instanceof BaseCaptureInterface) {
            if (CAMERA_POSITION_FRONT == (((BaseCaptureInterface) getActivity()).getCurrentCameraPosition())) {
                mImageView.setScaleX(-1);
            } else {
                mImageView.setScaleX(1);
            }
        }

        final View controlsFrame = view.findViewById(R.id.controlsFrame);
        final View topContainer = view.findViewById(R.id.top_container);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (view.getWidth() != 0 && view.getHeight() != 0) {
                    int height = view.getHeight() - view.getWidth();
                    ViewGroup.LayoutParams p = controlsFrame.getLayoutParams();
                    ViewGroup.LayoutParams p2 = topContainer.getLayoutParams();
                    p.height = height - height / 4;
                    p2.height = height / 4;
                    controlsFrame.setLayoutParams(p);
                    topContainer.setLayoutParams(p2);

                    Glide.with(getActivity()).load(Uri.parse(mOutputUri).getPath()).into(mImageView);

                    mRetry.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.back_white_btn));
                    mConfirm.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.confirm_photo_btn));
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.retry) mInterface.onRetry(mOutputUri);
        else if (v.getId() == R.id.confirm) mInterface.useMedia(mOutputUri);
    }
}
