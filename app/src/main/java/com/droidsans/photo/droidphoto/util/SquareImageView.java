package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Ong on 8/7/2015.
 */
public class SquareImageView extends ImageView {
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int length = widthMeasureSpec<heightMeasureSpec? widthMeasureSpec:heightMeasureSpec;
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
