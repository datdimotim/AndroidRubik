package com.dimotim.kubsolver.updatecheck;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SchedulerProvider {

    // UI thread
    public static Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }

    // IO thread
    public static Scheduler io() {
        return Schedulers.io();
    }

    // Computation thread
    public static Scheduler computation() {
        return Schedulers.computation();
    }
}