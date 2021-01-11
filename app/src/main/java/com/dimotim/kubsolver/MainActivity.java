package com.dimotim.kubsolver;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dimotim.kubsolver.shaderUtils.FileUtils;
import com.dimotim.kubsolver.updatecheck.CheckForUpdatesHttpClient;
import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.SchedulerProvider;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;
import com.sting_serializer.StringSerializer;

import java.io.IOException;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements SolveDialog.SolveListener {
    public static final String KUB_STATE="KUB_STATE";
    public static final String TAG="kubApp";
    private Bitmap bitmap;
    private GLSurfaceView glSurfaceView;
    public OpenGLRenderer renderer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Disposable disposable = HttpClient.getCheckForUpdateService()
                .getLatestRelease()
                .map(UpdatesUtil::parseCheckResultFromGithubResponse)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .subscribe(
                        success -> {
                            Toast.makeText(this, "new version: "+success.getTagName(), Toast.LENGTH_LONG).show();
                        },
                        error -> {
                            Toast.makeText(this, error.toString(),Toast.LENGTH_LONG).show();
                        }
                );


        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.texture);
        String vertexShaderText = FileUtils.readTextFromRaw(this, R.raw.vertexshader);
        final String fragmentShaderText = FileUtils.readTextFromRaw(this, R.raw.pixelshader);

        setContentView(R.layout.activity_main);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);

        OpenGLRenderer.State state=restoreState(savedInstanceState);
        renderer=new OpenGLRenderer(glSurfaceView, bitmap, vertexShaderText, fragmentShaderText, state, size -> {
            Log.i(TAG,"kub="+size);
            if(size==3){
                findViewById(R.id.buttonSolve).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonEdit).setVisibility(View.VISIBLE);
            }else if(size==2){
                findViewById(R.id.buttonSolve).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonEdit).setVisibility(View.GONE);
            }
            else {
                findViewById(R.id.buttonSolve).setVisibility(View.GONE);
                findViewById(R.id.buttonEdit).setVisibility(View.GONE);
            }
            Log.i(TAG,"state="+(findViewById(R.id.buttonSolve).getVisibility()==View.VISIBLE));
        });
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setOnTouchListener(renderer);


        LinearLayout layout= findViewById(R.id.panel);
        layout.addView(glSurfaceView);

        findViewById(R.id.buttonNewCube).setOnClickListener(v -> new DialogNewKub().show(getFragmentManager(),"DLG1"));
        findViewById(R.id.buttonShuffle).setOnClickListener(v -> new DialogAreYouSureShuffle().show(getFragmentManager(),"DLG2"));
        findViewById(R.id.buttonSolve).setOnClickListener(v -> new SolveDialog().show(getFragmentManager(), "DLG3"));
        findViewById(R.id.buttonEdit).setOnClickListener(v -> {
            Intent intent=new Intent();
            intent.setClass(MainActivity.this, SolverActivity.class);
            startActivityForResult(intent,1);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menu_benchmark1vs100){
            Intent intent=new Intent(this,BenchmarkActivity.class);
            intent.putExtra(BenchmarkActivity.THREADS,1);
            intent.putExtra(BenchmarkActivity.SIZE,100);
            startActivity(intent);
            return true;
        }
        if(item.getItemId()==R.id.menu_benchmark8vs1000){
            Intent intent=new Intent(this,BenchmarkActivity.class);
            intent.putExtra(BenchmarkActivity.THREADS,8);
            intent.putExtra(BenchmarkActivity.SIZE,1000);
            startActivity(intent);
            return true;
        }
        if(item.getItemId()==R.id.menu_benchmark1vs1000){
            Intent intent=new Intent(this,BenchmarkActivity.class);
            intent.putExtra(BenchmarkActivity.THREADS,1);
            intent.putExtra(BenchmarkActivity.SIZE,1000);
            startActivity(intent);
            return true;
        }
        if(item.getItemId()==R.id.menu_benchmark8vs10000){
            Intent intent=new Intent(this,BenchmarkActivity.class);
            intent.putExtra(BenchmarkActivity.THREADS,8);
            intent.putExtra(BenchmarkActivity.SIZE,10000);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,"OnActivityResult");
        if(data==null)return;
        if(data.getStringExtra(SolverActivity.PARAMS.RESULT.toString()).equals(SolverActivity.RESULT.CANCELED.toString()))return;
        int[][][] grani;
        try {
            grani=(int[][][]) StringSerializer.deserializeAtString(data.getStringExtra(SolverActivity.PARAMS.POSITION.toString()));
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Desserialization Error",e);
        }
        for(int[][] s:grani)for(int[] kv:s)for(int i=0;i<kv.length;i++)kv[i]++;
        renderer.setNewKub(grani);
    }

    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        glSurfaceView.onPause();
        //state=renderer.getState();
    }
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
        //if(state!=null){
            //renderer.setState(state);
            //state=null;
        //}
        glSurfaceView.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        String save= null;
        try {
            save = StringSerializer.serializeToString(renderer.getState());
            Log.i(TAG, "saved in preferences");
        } catch (IOException e) {
            Log.e(TAG,"saving in preferences error",e);
        }
        if(save!=null) {
            SharedPreferences preference = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = preference.edit();
            editor.putString(KUB_STATE, save);
            editor.apply();
        }
        bitmap.recycle();
    }

    private boolean supportES2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        OpenGLRenderer.State state=renderer.getState();
        outState.putSerializable(KUB_STATE, state);
    }

    private OpenGLRenderer.State restoreState(Bundle savedInstanceState) {
        OpenGLRenderer.State state = null;
        if (savedInstanceState != null) {
            state = (OpenGLRenderer.State) savedInstanceState.getSerializable(KUB_STATE);
            Log.i(TAG, "loaded in bundle");
        } else {
            SharedPreferences preferences = getPreferences(MODE_PRIVATE);
            String save = preferences.getString(KUB_STATE, null);
            if (save != null) {
                try {
                    state = (OpenGLRenderer.State) StringSerializer.deserializeAtString(save);
                    Log.i(TAG, "loaded in preferences");
                } catch (IOException | ClassNotFoundException e) {
                    Log.e(TAG, "loading in preferences error", e);
                }
            }
        }
        return state;
    }

    @Override
    public void onSolve(SolveDialog.SolveEntry solveEntry) {
        renderer.solve(solveEntry);
    }
}

