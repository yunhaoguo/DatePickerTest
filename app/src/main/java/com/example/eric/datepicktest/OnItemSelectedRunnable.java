package com.example.eric.datepicktest;

/**
 * Author: yunhaoguo
 * Date: 2019-09-06
 */
final class OnItemSelectedRunnable implements Runnable {
    final LoopView loopView;

    OnItemSelectedRunnable(LoopView loopview) {
        loopView = loopview;
    }

    @Override
    public final void run() {
        loopView.onItemSelectedListener.onItemSelected(loopView.items.get(loopView.getSelectedItem()));
    }
}
