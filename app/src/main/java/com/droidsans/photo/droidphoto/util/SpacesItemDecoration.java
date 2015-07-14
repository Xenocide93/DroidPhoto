package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.droidsans.photo.droidphoto.R;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    private Context context;

    public SpacesItemDecoration(Context context, int space) {
        this.space = space;
        this.context = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int colNum = context.getResources().getInteger(R.integer.main_feed_col_num);
        int itemCount = parent.getAdapter().getItemCount();
        outRect.left = space/2;
        outRect.right = space/2;
        outRect.bottom = space/2;
        outRect.top = space/2;

        if(position % colNum == 0){ //left edge
            outRect.left = 0;
        }
        if(position % colNum == colNum-1){ //right edge
            outRect.right = 0;
        }
        if(position < colNum){ //top edge
            outRect.top = 0;
        }
        if(position >= Math.floor((itemCount-1)/colNum)){ //bottom edge
            outRect.bottom = 0;
        }
    }
}