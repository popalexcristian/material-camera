package com.afollestad.materialcamera.internal;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

import java.io.FileOutputStream;
import java.io.IOException;

import static com.afollestad.materialcamera.internal.BaseCaptureActivity.CAMERA_POSITION_FRONT;
import static com.afollestad.materialcamera.util.ImageUtil.getExifDegreesFromJpeg;

public class StillshotPreviewFragment extends BaseGalleryFragment {

    private ImageView mImageView;
    private static Bitmap mBitmap;

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
                    p.height = height / 2;
                    p2.height = height / 2;
                    controlsFrame.setLayoutParams(p);
                    topContainer.setLayoutParams(p2);

                    mBitmap = getCroppedBitmap();
                    mImageView.setImageBitmap(mBitmap);

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
        if (v.getId() == R.id.retry) {
            mInterface.onRetry(mOutputUri);
        } else if (v.getId() == R.id.confirm) {

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(Uri.parse(mOutputUri).getPath());
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out); // bmp is your Bitmap instance
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

    private Bitmap getCroppedBitmap() {

        String path = Uri.parse(mOutputUri).getPath();
        final int rotationInDegrees = getExifDegreesFromJpeg(path);

        final Bitmap origBitmap = BitmapFactory.decodeFile(path);

        if (origBitmap == null) return null;

        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);

        Bitmap dstBmp;

        if (origBitmap.getWidth() >= origBitmap.getHeight()) {
            dstBmp = Bitmap.createBitmap(origBitmap, origBitmap.getWidth() / 2 - origBitmap.getHeight() / 2, 0, origBitmap.getHeight(), origBitmap.getHeight(), matrix, true);
        } else {
            dstBmp = Bitmap.createBitmap(origBitmap, 0, origBitmap.getHeight() / 2 - origBitmap.getWidth() / 2, origBitmap.getWidth(), origBitmap.getWidth(), matrix, true);
        }

        return dstBmp;
    }
}
