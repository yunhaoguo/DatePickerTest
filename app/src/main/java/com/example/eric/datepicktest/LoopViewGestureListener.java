package com.example.eric.datepicktest;

import android.view.MotionEvent;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
final class LoopViewGestureListener extends android.view.GestureDetector.SimpleOnGestureListener {

    final LoopView loopView;

    LoopViewGestureListener(LoopView loopview) {
        loopView = loopview;
    }

    @Override
    public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        loopView.scrollBy(velocityY);
        return true;
    }
}
