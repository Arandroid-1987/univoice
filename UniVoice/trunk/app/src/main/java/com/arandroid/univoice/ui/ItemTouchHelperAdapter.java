package com.arandroid.univoice.ui;

/**
 * Created by MeringoloRo on 03/11/2016.
 */
public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}