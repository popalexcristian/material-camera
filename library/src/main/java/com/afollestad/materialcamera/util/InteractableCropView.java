package com.afollestad.materialcamera.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.lyft.android.scissors.CropView;

/**
 * @author dorin
 * @since 10/11/2017
 */

public class InteractableCropView extends CropView {

    private boolean mCropEnabled;

    public InteractableCropView(Context context) {
        super(context);
    }

    public InteractableCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCropEnabled(boolean enabled) {
        mCropEnabled = enabled;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return mCropEnabled && super.dispatchTouchEvent(event);
    }
}
