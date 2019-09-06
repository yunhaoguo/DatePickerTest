package com.example.eric.datepicktest;

import java.lang.ref.WeakReference;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
final class SmoothScrollTimerTask implements Runnable {

    private int offset;
    private WeakReference<LoopView> loopViewRef;

    SmoothScrollTimerTask(LoopView loopview, int offset) {
        loopViewRef = new WeakReference<>(loopview);
        this.offset = offset;
    }

    @Override
    public final void run() {
        LoopView loopView = loopViewRef.get();
        if (loopView != null) {
            int realOffset = (int) ((float) offset * 0.1F);

            if (realOffset == 0) {
                if (offset < 0) {
                    realOffset = -1;
                } else {
                    realOffset = 1;
                }
            }
            if (Math.abs(offset) <= 0) {
                loopView.cancelFuture();
                loopView.handler.sendEmptyMessage(MessageHandler.WHAT_ITEM_SELECTED);
            } else {
                loopView.totalScrollY = loopView.totalScrollY + realOffset;
                loopView.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
                offset = offset - realOffset;
            }
        }
    }
}
