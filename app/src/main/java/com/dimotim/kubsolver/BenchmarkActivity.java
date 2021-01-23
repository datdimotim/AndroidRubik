package com.dimotim.kubsolver;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.Solution;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.NonConfigurationInstance;
import org.androidannotations.annotations.RootContext;
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
public class BenchmarkActivity extends AppCompatActivity {

    @ViewById(resName = "version")
    protected TextView versionTextView;

    @ViewById(resName = "progressBar")
    protected ProgressBar progressBar;

    @ViewById(resName = "text_view_percent")
    protected TextView percentTextView;

    @ViewById(resName = "title_text_view")
    protected TextView titleTextView;

    @Extra("THREADS")
    protected int threads;

    @Extra("SIZE")
    protected int size;

    @NonConfigurationInstance
    @Bean
    protected Benchmark benchmark;

    @InstanceState
    public boolean isStarted;

    @InstanceState
    public boolean isFinished;

    @InstanceState
    protected int progress;

    @InstanceState
    protected float timeSeconds;

    @InstanceState
    protected float avgLength;


    @AfterViews
    protected void init() {
        versionTextView.setText(versionTextView.getText()+BuildConfig.gitHash);

        updateProgress(progress);

        if (isFinished) {
            showResults(timeSeconds, avgLength);
            return;
        }

        if(!isStarted) {
            isStarted=true;
            benchmark.benchmark(threads, size);
        }
    }

    @Click(resName = "button_cancel")
    void onCancel() {
        benchmark.cancel();
        finish();
    }

    @UiThread
    void updateProgress(int percent){
        this.progress=percent;
        progressBar.setProgress(percent);
        percentTextView.setText(percent+"%");
    }

    @UiThread
    void showResults(float timeSeconds, float avgLength){
        isFinished=true;
        this.timeSeconds=timeSeconds;
        this.avgLength=avgLength;
        progressBar.setProgress(100);
        percentTextView.setText(100+"%");
        titleTextView.setText(
                "Total time="+timeSeconds+"s\n" +
                        "Avg size="+avgLength);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        benchmark.cancel();
    }
}

@EBean
class Benchmark {
    @RootContext
    protected BenchmarkActivity benchmarkActivity;

    @Bean
    protected Solvers solvers;

    private final AtomicBoolean isCancelled = new AtomicBoolean(false);

    @Background
    public void benchmark(int threads, int size){
        Log.d(Benchmark.class.getCanonicalName(), "threads="+threads+" count="+size);
        ExecutorService es=Executors.newFixedThreadPool(threads);
        final long timeStart=System.currentTimeMillis();
        AtomicInteger solutionLenght = new AtomicInteger(0);

        AtomicInteger progress = new AtomicInteger(0);

        List<Future<?>> tasks = IntStream.range(0,size)
                .mapToObj(i -> es.submit(()->{
                    if(isCancelled.get())return;
                    Solution solution = solvers.getKubSolver().solve(new Kub(true));

                    int curProg = progress.incrementAndGet();
                    benchmarkActivity.updateProgress((int)(100*curProg/(float) size));
                    solutionLenght.addAndGet(solution.getLength());
                }))
                .collect(Collectors.toList());

        try {
            for(Future<?> task:tasks){
                if(isCancelled.get()) return;
                task.get();
            }

            benchmarkActivity.showResults((System.currentTimeMillis()-timeStart)/(float)1000,solutionLenght.get()/(float) size);
        }catch (ExecutionException|InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            es.shutdownNow();
        }
    }

    public void cancel(){
        isCancelled.set(true);
    }
}

