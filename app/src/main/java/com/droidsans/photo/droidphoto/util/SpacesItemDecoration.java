package com.droidsans.photo.droidphoto.util;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.droidsans.photo.droidphoto.R;

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private Context context;
    private int colNum;
    private int space;
    private boolean topSpace, bottomSpace, leftSpace, rightSpace;

    public SpacesItemDecoration(Context context, int colNum, int space, boolean topSpace, boolean bottomSpace, boolean leftSpace, boolean rightSpace) {
        this.context = context;
        this.colNum = colNum;
        this.space = space;
        this.topSpace = topSpace;
        this.bottomSpace = bottomSpace;
        this.leftSpace = leftSpace;
        this.rightSpace = rightSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int itemCount = parent.getAdapter().getItemCount();
        outRect.left = space/2;
        outRect.right = space/2;
        outRect.bottom = space/2;
        outRect.top = space/2;

        if(position < colNum){ //top edge
            outRect.top = topSpace? space: 0;
        }
        if(position >= Math.floor((itemCount-1)/colNum)){ //bottom edge
            outRect.bottom = bottomSpace? space: 0;
        }
        if(position % colNum == 0){ //left edge
            outRect.left = leftSpace? space: 0;
        }
        if(position % colNum == colNum-1){ //right edge
            outRect.right = rightSpace? space: 0;
        }
    }
}