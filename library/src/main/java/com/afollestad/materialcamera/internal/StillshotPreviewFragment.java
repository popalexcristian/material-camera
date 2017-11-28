package com.afollestad.materialcamera.internal;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialcamera.R;
import com.afollestad.materialcamera.util.InteractableCropView;

import java.io.FileOutputStream;
import java.io.IOException;

import static com.afollestad.materialcamera.util.ImageUtil.getExifDegreesFromJpeg;

public class StillshotPreviewFragment extends BaseGalleryFragment {

    private InteractableCropView mImageView;
    private static Bitmap mBitmap;
    private boolean mIsCropping;
    private int mRequestedOrientation;
    private float mPreCropRatio;

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
        mImageView = view.findViewById(R.id.stillshot_imageview);

        mRetry.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        mCrop.setOnClickListener(this);

        final View controlsFrame = view.findViewById(R.id.controlsFrame);
        final View topContainer = view.findViewById(R.id.top_container);

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (view.getWidth() != 0 && view.getHeight() != 0) {
                    int height = view.getHeight() - view.getWidth();
                    ViewGroup.LayoutParams p = controlsFrame.getLayoutParams();
                    ViewGroup.LayoutParams p2 = topContainer.getLayoutParams();
                    p.height = height / 2;
                    p2.height = height / 2;
                    controlsFrame.setLayoutParams(p);
                    topContainer.setLayoutParams(p2);

                    mBitmap = getOriginalBitmap();
                    mImageView.setImageBitmap(mBitmap);

                    mRetry.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.back_white_btn));

                    mCrop.setVisibility(View.VISIBLE);
                    mCrop.setImageResource(R.drawable.crop_ic);

                    Drawable confirmBtnDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.btn_camera_checked);
                    String stringColor = mInterface.getAppColor();
                    if ((stringColor != null) && !stringColor.isEmpty()) {
                        int appColor = Color.parseColor(stringColor);
                        confirmBtnDrawable.setColorFilter(appColor, PorterDuff.Mode.SRC_ATOP);
                    }
                    mConfirm.setImageDrawable(confirmBtnDrawable);
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
    public void onResume() {
        super.onResume();
        mRequestedOrientation = getActivity().getRequestedOrientation();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().setRequestedOrientation(mRequestedOrientation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.crop) {
            setIsCropping(!mIsCropping);
        } else if (v.getId() == R.id.retry) {
            if (mIsCropping) {
                setIsCropping(false);
                mImageView.setViewportRatio(mPreCropRatio);
            } else {
                mInterface.onRetry(mOutputUri);
            }
        } else if (v.getId() == R.id.confirm) {

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(Uri.parse(mOutputUri).getPath());
                mImageView.crop().compress(Bitmap.CompressFormat.JPEG, 90, out); // bmp is your Bitmap instance
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        mInterface.useMedia(mOutputUri);
                        out.close();
                    }
                } catch (IOException e) {
                    mInterface.onRetry(mOutputUri);
                    e.printStackTrace();
                }
            }
        }
    }

    private void setIsCropping(boolean isCropping) {
        mIsCropping = isCropping;
        mImageView.setCropEnabled(isCropping);
        mPreCropRatio = mImageView.getViewportRatio();
        mConfirm.setVisibility(isCropping ? View.GONE : View.VISIBLE);
        mWhiteView.setVisibility(isCropping ? View.GONE : View.VISIBLE);
        mCrop.setImageResource(isCropping ? R.drawable.checkmark_ic : R.drawable.crop_ic);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            try {
                mBitmap.recycle();
                mBitmap = null;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private Bitmap getOriginalBitmap() {
        String path = Uri.parse(mOutputUri).getPath();
        final int rotationInDegrees = getExifDegreesFromJpeg(path);

        final Bitmap origBitmap = BitmapFactory.decodeFile(path);

        if (origBitmap == null) return null;

        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);

        return Bitmap.createBitmap(origBitmap, 0, 0, origBitmap.getWidth(), origBitmap.getHeight(), matrix, true);
    }
}
