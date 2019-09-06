package com.example.eric.datepicktest;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
final class MessageHandler extends Handler {
    static final int WHAT_INVALIDATE_LOOP_VIEW = 1000;
    static final int WHAT_SMOOTH_SCROLL = 2000;
    static final int WHAT_ITEM_SELECTED = 3000;

    private @Nullable
    WeakReference<LoopView> loopViewRef;

    MessageHandler(LoopView loopview) {
        loopViewRef = new WeakReference<LoopView>(loopview);
    }

    @Override
    public final void handleMessage(Message msg) {
        if (loopViewRef != null) {
            LoopView loopView = loopViewRef.get();
            if (loopView != null) {
                switch (msg.what) {
                    case WHAT_INVALIDATE_LOOP_VIEW:
                        loopView.invalidate();
                        break;
                    case WHAT_SMOOTH_SCROLL:
                        loopView.smoothScroll(LoopView.ACTION.FLING);
                        break;
                    case WHAT_ITEM_SELECTED:
                        loopView.onItemSelected();
                        break;
                }
            }
        }
    }

}
