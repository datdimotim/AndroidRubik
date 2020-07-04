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
import com.dimotim.kubSolver.solvers.SimpleSolver1;
import com.dimotim.kubSolver.solvers.SimpleSolver2;
import com.dimotim.kubSolver.tables.SymTables;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


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
    private final KubSolver<SymTables.KubState,SimpleSolver1.SolveState<SymTables.KubState>> kubSolver=
            new KubSolver<>(SymTables.readTables(),
                    new SimpleSolver1<SymTables.KubState>(),
                    new SimpleSolver2<SymTables.KubState>());

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
        int solutionLenght=0;

        final ArrayList<Callable<Integer>> taskList=new ArrayList<>(size);
        for(int i = 0; i< size; i++)taskList.add(new Callable<Integer>() {
            private final Kub kub=new Kub(true);
            @Override
            public Integer call(){
                return kubSolver.solve(kub).getLength();
            }
        });

        ArrayList<Future<Integer>> resultList=new ArrayList<>(size);
        for(Callable<Integer> task:taskList)resultList.add(es.submit(task));

        try {
            for(int i = 0; i< size; i++){
                if(isCancelled())break;
                solutionLenght+=resultList.get(i).get();
                publishProgress((int)(100*i/(float) size));
            }
        }catch (ExecutionException|InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            es.shutdownNow();
        }

        return new float[]{(System.currentTimeMillis()-timeStart)/(float)1000,solutionLenght/(float) size};
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

