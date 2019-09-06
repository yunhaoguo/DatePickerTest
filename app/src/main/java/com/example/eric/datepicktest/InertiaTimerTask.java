package com.example.eric.datepicktest;

import java.lang.ref.WeakReference;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
final class InertiaTimerTask implements Runnable {

    private float velocity;
    private final WeakReference<LoopView> loopViewRef;

    InertiaTimerTask(LoopView loopview, float velocityY) {
        super();
        loopViewRef = new WeakReference<>(loopview);
        if (Math.abs(velocityY) > 2000F) {
            if (velocityY > 0.0F) {
                velocity = 2000F;
            } else {
                velocity = -2000F;
            }
        } else {
            velocity = velocityY;
        }
    }

    @Override
    public final void run() {
        LoopView loopView = loopViewRef.get();
        if (loopView == null) {
            return;
        }
        if (Math.abs(velocity) >= 0.0F && Math.abs(velocity) <= 20F) {
            loopView.cancelFuture();
            loopView.handler.sendEmptyMessage(MessageHandler.WHAT_SMOOTH_SCROLL);
            return;
        }
        int tempOffset = (int) (velocity / 100F);
        loopView.totalScrollY = loopView.totalScrollY - tempOffset;
        if (!loopView.isLoop) {
            loopView.totalScrollY = (int) Math.max(loopView.getMinScrollY(), loopView.totalScrollY);
            loopView.totalScrollY = (int) Math.min(loopView.getMaxScrollY(), loopView.totalScrollY);
        }
        if (velocity < 0.0F) {
            velocity = velocity + 20F;
        } else {
            velocity = velocity - 20F;
        }
        loopView.handler.sendEmptyMessage(MessageHandler.WHAT_INVALIDATE_LOOP_VIEW);
    }
}
