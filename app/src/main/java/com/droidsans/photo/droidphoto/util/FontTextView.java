package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.droidsans.photo.droidphoto.R;

public class FontTextView extends TextView {
    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        if (isInEditMode()) {return;}
        String font = "Roboto-Medium.ttf";
        if (attrs != null) {
            // Look up any layout-defined attributes
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontTextView);
            for (int i = 0; i < a.getIndexCount(); i++) {
                int attr = a.getIndex(i);
                switch (attr) {
                    case R.styleable.FontTextView_customFont:
                        font = a.getString(attr);
                        break;
                }
            }
            a.recycle();
        }
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/" + font));
    }

    public void setCustomFont(Context context, String fontName) {
        if (isInEditMode()) {return;}
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/" + fontName));
    }
}
