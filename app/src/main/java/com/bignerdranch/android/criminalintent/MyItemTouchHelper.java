package com.bignerdranch.android.criminalintent;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

import java.util.Collections;


/**
 * Created by Administrator on 2017/11/8.
 */

public class MyItemTouchHelper extends ItemTouchHelper.Callback {
    private  int dragFlags = 0;
    private  int swipeFlags = 0;


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        dragFlags = 0;
        swipeFlags = 0;
        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        } else {
            dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            //if (viewHolder.getAdapterPosition() != 0)
            //  swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        }
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (toPosition != 0) {
            if (fromPosition < toPosition)
                //向下拖动
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mListData, i, i + 1);
                }
            else {
                //向上拖动
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mListData, i, i - 1);
                }
            }
            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
        }
        return true;

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
