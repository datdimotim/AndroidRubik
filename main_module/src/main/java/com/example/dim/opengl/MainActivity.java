package com.example.dim.opengl;

import android.app.Activity;
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

import com.example.dim.opengl.shaderUtils.FileUtils;
import com.example.dimotim.sqarebuttons.SolverActivity;
import com.sting_serializer.StringSerializer;

import java.io.IOException;



public class MainActivity extends AppCompatActivity{
    public static final String KUB_STATE="KUB_STATE";
    public static final String TAG="kubApp";
    private Bitmap bitmap;
    private GLSurfaceView glSurfaceView;
    public OpenGLRenderer renderer;
    //private OpenGLRenderer.State state;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        if (!supportES2()) {
            Toast.makeText(this, "OpenGl ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.texture);
        String vertexShaderText = FileUtils.readTextFromRaw(this, R.raw.vertexshader);
        final String fragmentShaderText = FileUtils.readTextFromRaw(this, R.raw.pixelshader);

        setContentView(R.layout.activity_main);

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);

        OpenGLRenderer.State state=restoreState(savedInstanceState);
        renderer=new OpenGLRenderer(glSurfaceView, bitmap, vertexShaderText, fragmentShaderText, state, new OpenGLRenderer.KubChangeListener() {
            @Override
            public void kubChanged(int size) {
                Log.i(TAG,"kub="+size);
                if(size==3){
                    findViewById(R.id.buttonSolve).setVisibility(View.VISIBLE);
                    findViewById(R.id.buttonEdit).setVisibility(View.VISIBLE);
                }
                else {
                    findViewById(R.id.buttonSolve).setVisibility(View.GONE);
                    findViewById(R.id.buttonEdit).setVisibility(View.GONE);
                }
                Log.i(TAG,"state="+(findViewById(R.id.buttonSolve).getVisibility()==View.VISIBLE));
            }
        });
        glSurfaceView.setRenderer(renderer);

        glSurfaceView.setOnTouchListener(renderer);


        LinearLayout layout=(LinearLayout)findViewById(R.id.panel);
        layout.addView(glSurfaceView);

        findViewById(R.id.buttonNewCube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogNewKub().show(getFragmentManager(),"DLG1");
            }
        });
        findViewById(R.id.buttonShuffle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DialogAreYouSureShuffle().show(getFragmentManager(),"DLG2");
            }
        });
        findViewById(R.id.buttonSolve).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renderer.solve();
            }
        });
        findViewById(R.id.buttonEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setClass(MainActivity.this, SolverActivity.class);
                startActivityForResult(intent,1);
            }
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
        if(item.getItemId()==R.id.menu_benchmark){
            startActivity(new Intent(this,BenchmarkActivity.class));
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
}

