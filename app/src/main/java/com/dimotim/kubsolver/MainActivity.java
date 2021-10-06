package com.dimotim.kubsolver;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.dimotim.kubsolver.dialogs.DialogAreYouSureShuffle;
import com.dimotim.kubsolver.dialogs.DialogNewKub;
import com.dimotim.kubsolver.dialogs.PreferencesDialog;
import com.dimotim.kubsolver.dialogs.QRCodeAlertDialog;
import com.dimotim.kubsolver.dialogs.SolveDialog;
import com.dimotim.kubsolver.dialogs.VersionInfoDialog;
import com.dimotim.kubsolver.dialogs.YesNoDialog;
import com.dimotim.kubsolver.services.CheckForUpdatesManager;
import com.dimotim.kubsolver.services.UpdateCheckSharedPreferencesLog;
import com.dimotim.kubsolver.shaderUtils.FileUtils;
import com.dimotim.kubsolver.updatecheck.HttpClient;
import com.dimotim.kubsolver.updatecheck.UpdatesUtil;
import com.dimotim.kubsolver.updatecheck.model.CheckResult;
import com.dimotim.kubsolver.updatecheck.model.ReleaseModel;
import com.dimotim.kubsolver.util.StringSerializer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.HashMap;
import java.util.Map;

import lombok.SneakyThrows;
import lombok.Value;

@EActivity(resName = "activity_main")
@OptionsMenu(resName = "main_menu")
public class MainActivity extends Activity implements SolveDialog.SolveListener {
    public static final int SOLVER_ACTIVITY_RESULT_CODE = 1;
    public static final String KUB_STATE="KUB_STATE";
    public static final String TAG=MainActivity.class.getCanonicalName();

    @ViewById(resName = "panel")
    protected GLSurfaceView glSurfaceView;

    @ViewById(resName = "buttonSolve")
    protected Button buttonSolve;

    @ViewById(resName = "buttonEdit")
    protected Button buttonEdit;

    public OpenGLRenderer renderer;

    private State savedInitialState;

    @Pref
    protected KubPreferences_ kubPreferences;

    @Bean
    protected Solvers solvers;

    @Bean
    protected UpdatesUtil updatesUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInitialState= restoreState(savedInstanceState);
    }

    @AfterViews
    protected void setup() {
        Log.i(TAG,"onCreate");
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if(kubPreferences.checkForUpdates().getOr(true)) {
            CheckForUpdatesManager.setupCheckForUpdates(this);
        } else {
            CheckForUpdatesManager.cancelCheckForUpdates(this);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.texture);
        final String vertexShaderText = FileUtils.readTextFromRaw(this, R.raw.vertexshader);
        final String fragmentShaderText = FileUtils.readTextFromRaw(this, R.raw.pixelshader);

        glSurfaceView.setEGLContextClientVersion(2);

        State state = savedInitialState;
        renderer=new OpenGLRenderer(glSurfaceView, bitmap, vertexShaderText, fragmentShaderText, state, size -> {
            boolean buttonSolveVisible = size == 3 || size == 2;
            boolean buttonEditVisible = size == 3;
            buttonSolve.setVisibility(buttonSolveVisible ? View.VISIBLE : View.GONE);
            buttonEdit.setVisibility(buttonEditVisible ? View.VISIBLE : View.GONE);
        },solvers);
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
        SolverActivity_.intent(this).startForResult(SOLVER_ACTIVITY_RESULT_CODE);
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

    @OptionsItem(resName = "menu_preferences")
    void openPreferences() {
        PreferencesDialog.showDialog(this);
    }

    @OptionsItem(resName = {
            "menu_benchmark1vs100",
            "menu_benchmark8vs1000",
            "menu_benchmark1vs1000",
            "menu_benchmark8vs10000",
    })
    void menuBenchmark(MenuItem item){
        BenchmarkConfig config = benchmarkParams.get(item.getItemId());

        BenchmarkActivity_.intent(this)
                .threads(config.getThreads())
                .size(config.getCount())
                .start();
    }

    @OptionsItem(resName = "menu_qr_code")
    @Background
    void menuQrCode(){
        try {
            ReleaseModel releaseModel = HttpClient.getCheckForUpdateService()
                    .getLatestRelease()
                    .execute()
                    .body();

            CheckResult success = UpdatesUtil.parseCheckResultFromGithubResponse(releaseModel);

            ContextCompat.getMainExecutor(this).execute(() -> {
                QRCodeAlertDialog.showDialog(this, success.getHtmlUrl());
            });
        } catch (Exception e) {
            ContextCompat.getMainExecutor(this).execute(() -> {
                Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show();
                Log.e(MainActivity.class.getCanonicalName(), e.toString(), e);
            });
        }
    }

    @OptionsItem(resName = "menu_check_for_updates")
    @Background
    void menuCheckForUpdates(){
        try {
            ReleaseModel releaseModel = HttpClient.getCheckForUpdateService()
                    .getLatestRelease()
                    .execute()
                    .body();

            CheckResult success = UpdatesUtil.parseCheckResultFromGithubResponse(releaseModel);

            ContextCompat.getMainExecutor(this).execute(() -> {
                boolean isSameVer = updatesUtil.isSameVersion(success);
                new UpdateCheckSharedPreferencesLog(this).updateTimeLastSuccessCheck();
                if(isSameVer){
                    Toast.makeText(this, "This version is actual",Toast.LENGTH_LONG).show();
                    return;
                }

                YesNoDialog.showDialog(this, "New version "+success.getTagName()+" available, install update?", ()->{
                    OpenUrlIntent.showDialog(this, success.getHtmlUrl());
                });
            });
        } catch (Exception e) {
            ContextCompat.getMainExecutor(this).execute(() -> {
                Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show();
                Log.e(MainActivity.class.getCanonicalName(), e.toString(), e);
            });
        }
    }

    @OptionsItem(resName = "menu_version_info")
    void menuVersionInfo(){
        VersionInfoDialog.showDialog(this);
    }

    @OnActivityResult(SOLVER_ACTIVITY_RESULT_CODE)
    @SneakyThrows
    protected void onActivityResult(Intent data) {
        Log.i(TAG,"OnActivityResult");
        if(data==null)return;
        if(data.getStringExtra(SolverActivity.PARAMS.RESULT.toString()).equals(SolverActivity.RESULT.CANCELED.toString()))return;
        int[][][] grani=(int[][][]) StringSerializer.deserializeFromString(data.getStringExtra(SolverActivity.PARAMS.POSITION.toString()));
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
        String save = StringSerializer.serializeToString(renderer.getState());
        Log.i(TAG, "saved in preferences");
        kubPreferences.edit()
                .kubState()
                .put(save)
                .apply();
    }

    private boolean supportES2() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        State state=renderer.getState();
        outState.putSerializable(KUB_STATE, state);
    }

    private State restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            State state = (State) savedInstanceState.getSerializable(KUB_STATE);
            Log.i(TAG, "loaded in bundle");
            return state;
        } else {
            String save = kubPreferences.kubState().getOr(null);
            if (save == null) return null;
            try {
                State state = (State) StringSerializer.deserializeFromString(save);
                Log.i(TAG, "loaded in preferences");
                return state;
            } catch (Exception e) {
                Log.w(TAG, e);
                return null;
            }
        }
    }

    @Override
    public void onSolve(SolveDialog.SolveEntry solveEntry) {
        renderer.solve(solveEntry);
    }
}

