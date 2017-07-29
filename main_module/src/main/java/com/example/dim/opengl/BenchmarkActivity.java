package com.example.dim.opengl;
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


public class BenchmarkActivity extends AppCompatActivity {
    private Benchmark benchmark;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.benchmark_layout);
        benchmark=new Benchmark(this);
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

class Benchmark extends AsyncTask<Void,Integer,int[]>{
    private volatile Controls controls;
    private final KubSolver<SymTables.KubState,SimpleSolver1.SolveState<SymTables.KubState>> kubSolver=
            new KubSolver<>(SymTables.readTables(),
                    new SimpleSolver1<SymTables.KubState>(),
                    new SimpleSolver2<SymTables.KubState>());
    final Kub kub=new Kub(false);

    Benchmark(AppCompatActivity activity){
        link(activity);
    }

    void link(AppCompatActivity activity){
        controls=new Controls(activity);
    }

    void unlink(){
        controls=null;
    }

    @Override
    protected void onPreExecute() {
        onProgressUpdate(0);
    }

    @Override
    protected int[] doInBackground(Void... params) {
        final long timeStart=System.currentTimeMillis();
        int lenght=0;
        for(int i=0;i<100;i++){
            if(isCancelled())return new int[]{0,0};
            kub.randomPos();
            lenght+=kubSolver.solve(kub).length;
            publishProgress(i);
        }
        return new int[]{(int)(System.currentTimeMillis()-timeStart)/1000,lenght/100};
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Controls controls=this.controls;
        if(controls==null)return;
        controls.progressBar.setProgress(values[0]);
        controls.percent.setText(values[0]+"%");
    }

    @Override
    protected void onCancelled() {
        Controls controls=this.controls;
        if(controls==null)return;
        controls.activity.finish();
    }

    @Override
    protected void onPostExecute(int[] ints) {
        Controls controls=this.controls;
        if (controls==null)return;
        controls.progressBar.setProgress(100);
        controls.percent.setText(100+"%");
        controls.title.setText("Time="+ints[0]+" seconds, avg length="+ints[1]);
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

