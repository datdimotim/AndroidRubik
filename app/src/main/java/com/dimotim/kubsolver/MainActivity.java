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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dimotim.kubsolver.dialogs.DialogAreYouSureShuffle;
import com.dimotim.kubsolver.dialogs.DialogNewKub;
import com.dimotim.kubsolver.dialogs.QRCodeAlertDialog;
import com.dimotim.kubsolver.dialogs.SolveDialog;
import com.dimotim.kubsolver.dialogs.YesNoDialog;
import com.dimotim.kubsolver.services.BootReceiver;
import com.dimotim.kubsolver.services.CheckUpdateReceiver;
import com.dimotim.kubsolver.shaderUtils.FileUtils;
import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.SchedulerProvider;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;
import com.sting_serializer.StringSerializer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import lombok.Value;

@EActivity(resName = "activity_main")
@OptionsMenu(resName = "main_menu")
public class MainActivity extends AppCompatActivity implements SolveDialog.SolveListener {
    public static final String KUB_STATE="KUB_STATE";
    public static final String TAG=MainActivity.class.getCanonicalName();

    @ViewById(resName = "panel")
    protected GLSurfaceView glSurfaceView;
    public OpenGLRenderer renderer;

    @AfterViews
    protected void setup() {
        Log.i(TAG,"onCreate");
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        BootReceiver.enableBootReceiver(this);
        CheckUpdateReceiver.setupRepeatingCheck(this);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.texture);
        final String vertexShaderText = FileUtils.readTextFromRaw(this, R.raw.vertexshader);
        final String fragmentShaderText = FileUtils.readTextFromRaw(this, R.raw.pixelshader);

        glSurfaceView.setEGLContextClientVersion(2);

        OpenGLRenderer.State state= null;//restoreState(savedInstanceState);
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
    }

    @Touch(resName = "panel")
    void onTouchSurfaceView(View v, MotionEvent event){
        renderer.onTouch(v, event);
    }


    @Click(resName = "buttonNewCube")
    void onButtonNewCube(){
        new DialogNewKub().show(getFragmentManager(),"DLG1");
    }

    @Click(resName = "buttonShuffle")
    void onButtonShuffle(){
        new DialogAreYouSureShuffle().show(getFragmentManager(),"DLG2");
    }

    @Click(resName = "buttonSolve")
    void onButtonSolve(){
        new SolveDialog().show(getFragmentManager(), "DLG3");
    }

    @Click(resName = "buttonEdit")
    void onButtonEdit(){
        Intent intent=new Intent();
        intent.setClass(MainActivity.this, SolverActivity.class);
        startActivityForResult(intent,1);
    }

    private final Map<Integer, BenchmarkConfig> benchmarkParams = new HashMap<Integer, BenchmarkConfig>(){{
        put(R.id.menu_benchmark1vs100, new BenchmarkConfig(1,100));
        put(R.id.menu_benchmark8vs1000, new BenchmarkConfig(8,1000));
        put(R.id.menu_benchmark1vs1000, new BenchmarkConfig(1,1000));
        put(R.id.menu_benchmark8vs10000, new BenchmarkConfig(8,10000));
    }};

    @Value
    private static class BenchmarkConfig{
        int threads;
        int count;
    }

    @OptionsItem(resName = {
            "menu_benchmark1vs100",
            "menu_benchmark8vs1000",
            "menu_benchmark1vs1000",
            "menu_benchmark8vs10000",
    })
    void menuBenchmark(MenuItem item){
        BenchmarkConfig config = benchmarkParams.get(item.getItemId());
        Intent intent=new Intent(this,BenchmarkActivity.class);
        intent.putExtra(BenchmarkActivity.THREADS,config.getThreads());
        intent.putExtra(BenchmarkActivity.SIZE,config.getCount());
        startActivity(intent);
    }

    @OptionsItem(resName = "menu_qr_code")
    void menuQrCode(){
        Disposable disposable = HttpClient.getCheckForUpdateService()
                .getLatestRelease()
                .map(UpdatesUtil::parseCheckResultFromGithubResponse)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .subscribe(
                        success -> QRCodeAlertDialog.showDialog(this, success.getHtmlUrl()),
                        error -> Toast.makeText(this, error.toString(),Toast.LENGTH_LONG).show()
                );
    }

    @OptionsItem(resName = "menu_check_for_updates")
    void menuCheckForUpdates(){
        Disposable disposable = HttpClient.getCheckForUpdateService()
                .getLatestRelease()
                .map(UpdatesUtil::parseCheckResultFromGithubResponse)
                .observeOn(SchedulerProvider.ui())
                .subscribeOn(SchedulerProvider.io())
                .subscribe(
                        success -> {
                            if(UpdatesUtil.isSameVersion(success)){
                                Toast.makeText(this, "This version is actual",Toast.LENGTH_LONG).show();
                                return;
                            }

                            YesNoDialog.showDialog(this, "New version "+success.getTagName()+" available, install update?", ()->{
                                OpenUrlIntent.showDialog(this, success.getHtmlUrl());
                            });

                        },
                        error -> {
                            Toast.makeText(this, error.toString(),Toast.LENGTH_LONG).show();
                            Log.d(MainActivity.class.getCanonicalName(), error.toString());
                        }
                );
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

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume");
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

