package com.dimotim.kubsolver;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dimotim.kubSolver.Kub;
import com.dimotim.kubSolver.KubSolver;
import com.dimotim.kubSolver.Solution;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class BenchmarkActivity extends AppCompatActivity {
    public static final String TAG="BenchmarkActivity: ";
    public static final String THREADS="THREADS";
    public static final String SIZE="SIZE";
    private Benchmark benchmark;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.benchmark_layout);
        Intent intent=getIntent();
        final int threads=intent.getIntExtra(THREADS,-1);
        if(threads==-1)throw new RuntimeException();
        final int size=intent.getIntExtra(SIZE,-1);
        if(size==-1)throw new RuntimeException();
        benchmark=new Benchmark(this,threads,size);
        TextView versionTextView= findViewById(R.id.version);
        versionTextView.setText(versionTextView.getText()+BuildConfig.gitHash);
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                benchmark.cancel(false);
                finish();
            }
        });

        benchmark.execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        benchmark.cancel(false);
    }
}

class Benchmark extends AsyncTask<Void,Integer,float[]>{
    private final ExecutorService es;
    private final int size;
    private final Controls controls;
    private final KubSolver<?,?> kubSolver= Solvers.getSolvers().kubSolver;

    Benchmark(AppCompatActivity activity,int threads,int size){
        this.size=size;
        es=Executors.newFixedThreadPool(threads);
        controls=new Controls(activity);
    }

    @Override
    protected void onPreExecute() {
        onProgressUpdate(0);
    }

    @Override
    protected float[] doInBackground(Void... params) {
        final long timeStart=System.currentTimeMillis();
        AtomicInteger solutionLenght = new AtomicInteger(0);

        AtomicInteger progress = new AtomicInteger(0);

        List<Future<Solution>> tasks = IntStream.range(0,size)
                .mapToObj(i -> es.submit(()->{
                    Solution solution = kubSolver.solve(new Kub(true));

                    int curProg = progress.incrementAndGet();
                    publishProgress((int)(100*curProg/(float) size));
                    solutionLenght.addAndGet(solution.getLength());
                    return solution;
                }))
                .collect(Collectors.toList());

        try {
            for(Future<Solution> task:tasks)task.get();
            return new float[]{(System.currentTimeMillis()-timeStart)/(float)1000,solutionLenght.get()/(float) size};
        }catch (ExecutionException|InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            es.shutdownNow();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        controls.progressBar.setProgress(values[0]);
        controls.percent.setText(values[0]+"%");
    }

    @Override
    protected void onPostExecute(float[] floats) {
        if(isCancelled()){
            controls.activity.finish();
            return;
        }
        controls.progressBar.setProgress(100);
        controls.percent.setText(100+"%");
        controls.title.setText(
                        "Total time="+floats[0]+"s\n" +
                        "Avg size="+floats[1]);
    }

    private static class Controls{
        final AppCompatActivity activity;
        final ProgressBar progressBar;
        final TextView percent;
        final TextView title;
        Controls(AppCompatActivity activity){
            this.activity=activity;
            progressBar= (ProgressBar) activity.findViewById(R.id.progressBar);
            percent=(TextView)activity.findViewById(R.id.text_view_percent);
            title=(TextView)activity.findViewById(R.id.title_text_view);
        }
    }
}

