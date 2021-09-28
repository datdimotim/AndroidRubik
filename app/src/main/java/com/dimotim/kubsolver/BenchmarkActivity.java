package com.dimotim.kubsolver;

import android.app.Activity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Solution;
import com.dimotim.kubsolver.services.UpdateCheckSharedPreferencesLog;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EActivity(resName = "benchmark_layout")
public class BenchmarkActivity extends Activity {

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    @Bean
    protected Solvers solvers;

    @Bean
    protected GitVersionInfo gitVersionInfo;

    @ViewById(resName = "version")
    protected TextView versionTextView;

    @ViewById(resName = "progressBar")
    protected ProgressBar progressBar;

    @ViewById(resName = "text_view_percent")
    protected TextView percentTextView;

    @ViewById(resName = "title_text_view")
    protected TextView titleTextView;

    @ViewById(resName = "lastCheckForUpdates")
    protected TextView lastUpdateCheck;

    @ViewById(resName = "preLastCheckForUpdates")
    protected TextView preLastUpdateCheck;

    @Extra("THREADS")
    protected int threads;

    @Extra("SIZE")
    protected int size;

    @AfterViews
    protected void init() {
        versionTextView.setText(versionTextView.getText() + gitVersionInfo.getGitHash());
        lastUpdateCheck.setText(lastUpdateCheck.getText() + new UpdateCheckSharedPreferencesLog(this).getUpdateTimeLastSuccessCheck());
        preLastUpdateCheck.setText(preLastUpdateCheck.getText() + new UpdateCheckSharedPreferencesLog(this).getUpdateTimePreLastSuccessCheck());
        isCancelled.set(false);
        benchmark();
    }

    @Click(resName = "button_cancel")
    void onCancel() {
        isCancelled.set(true);
        finish();
    }

    @Background
    void benchmark(){
        Log.d(BenchmarkActivity.class.getCanonicalName(), "threads="+threads+" count="+size);
        ExecutorService es=Executors.newFixedThreadPool(threads);
        updateProgress(0);
        final long timeStart=System.currentTimeMillis();
        AtomicInteger solutionLenght = new AtomicInteger(0);

        AtomicInteger progress = new AtomicInteger(0);

        List<Future<?>> tasks = IntStream.range(0,size)
                .mapToObj(i -> es.submit(()->{
                    if(isCancelled.get())return;
                    Solution solution = solvers.getKubSolver().solve(new Kub(true));

                    int curProg = progress.incrementAndGet();
                    updateProgress((int)(100*curProg/(float) size));
                    solutionLenght.addAndGet(solution.getLength());
                }))
                .collect(Collectors.toList());

        try {
            for(Future<?> task:tasks){
                if(isCancelled.get()) return;
                task.get();
            }

            showResults((System.currentTimeMillis()-timeStart)/(float)1000,solutionLenght.get()/(float) size);
        }catch (ExecutionException|InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            es.shutdownNow();
        }
    }

    @UiThread
    void updateProgress(int percent){
        progressBar.setProgress(percent);
        percentTextView.setText(percent+"%");
    }

    @UiThread
    void showResults(float timeSeconds, float avgLength){
        progressBar.setProgress(100);
        percentTextView.setText(100+"%");
        titleTextView.setText(
                "Total time="+timeSeconds+"s\n" +
                        "Avg size="+avgLength);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCancelled.set(true);
    }
}

