package com.example.dim.opengl;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.dim.opengl.kub.Kub;
import com.example.dim.opengl.kub.State;
import com.example.dim.opengl.kub.to_solver_interface.FormatConverter;
import com.example.dim.opengl.shaderUtils.TextureUtils;

import java.io.Serializable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import kub.kubSolver.InvalidPositionException;
import kub.kubSolver.KubSolver;
import kub.kubSolver.Solution;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;

public class OpenGLRenderer implements Renderer,View.OnTouchListener{
    public static final String TAG="renderer: ";
    private SimpleShaderProgram shaderProgram;
    private final float[] viewMatrix = new float[16];
    private final float[] monitorMatrix=new float[16];
    private float width;
    private float height;
    private final Bitmap bitmap;
    private final String fragmentShaderText;
    private final String vertexShaderText;
    private Kub kub;
    private final GLSurfaceView glSurfaceView;
    private final KubChangeListener kubChangeListener;
    public interface KubChangeListener{
        void kubChanged(int size);
    }

    public OpenGLRenderer(GLSurfaceView glSurfaceView,
                          Bitmap bitmap,
                          String vertexShaderText,
                          String fragmentShaderText,
                          State saveState,
                          KubChangeListener kubChangeListener) {
        this.kubChangeListener=kubChangeListener;
        this.glSurfaceView = glSurfaceView;
        this.bitmap = bitmap;
        this.fragmentShaderText=fragmentShaderText;
        this.vertexShaderText=vertexShaderText;
        Matrix.setIdentityM(viewMatrix, 0);

        if(saveState!=null) {
            kub = new Kub(saveState.kubState);
            kubChangeListener.kubChanged(saveState.kubState.getN());
            System.arraycopy(saveState.viewMatrix, 0, viewMatrix, 0, 16);
        }
        else{
            kub=new Kub(3);
            kubChangeListener.kubChanged(3);
        }
    }
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        Log.i(TAG,"onSurface created");
        glClearColor(0.7f, 0.7f, 0.7f, 1f);
        glEnable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0);
        shaderProgram=new SimpleShaderProgram(vertexShaderText,fragmentShaderText);
        glUseProgram(shaderProgram.shaderProgramId);
        int textureImageId = TextureUtils.loadTextureFromBitmap(bitmap);
        kub.shaderProgramChanged(shaderProgram);

        glBindTexture(GL_TEXTURE_2D, textureImageId);
        glUniform1i(shaderProgram.textureId, 0);// 0- индекс текстурного блока
    }
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        Log.i(TAG,"onSurface changed");
        glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        Matrix.setIdentityM(monitorMatrix, 0);
        if(width<height)Matrix.scaleM(monitorMatrix, 0, 1.0f, this.width / this.height, 1.0f);
        else            Matrix.scaleM(monitorMatrix, 0, this.height / this.width, 1.0f, 1.0f);
        Matrix.scaleM(monitorMatrix, 0, 0.5f, 0.5f, 0.5f);
        glUniformMatrix4fv(shaderProgram.monitorMatrixId, 1, false, monitorMatrix, 0);
    }
    public void onDrawFrame(GL10 arg0) {
        glUniformMatrix4fv(shaderProgram.viewMatrixId, 1, false, viewMatrix, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        glClear(GL_DEPTH_BUFFER_BIT);
        kub.draw();
    }
    public State getState(){
        final State[] state=new State[1];
        final Object sync=new Object();
        synchronized (sync){
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    float[] matSave=new float[16];
                    System.arraycopy(viewMatrix,0,matSave,0,16);
                    com.example.dim.opengl.kub.State kubState=kub.getState();
                    state[0]=new State(kubState,matSave);
                    synchronized (sync){
                        sync.notifyAll();
                    }
                }
            });
            try {
                sync.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return state[0];
    }

    public void setState(final State state){
        kubChangeListener.kubChanged(state.kubState.getN());
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                kub=new Kub(state.kubState);
                System.arraycopy(state.viewMatrix,0, viewMatrix,0,16);
                kub.shaderProgramChanged(shaderProgram);
            }
        });
    }
    private float[] toOpenglOutCoord(float x,float y){
        float[] coord=new float[2];
        coord[0]=-1f+x*2/width;
        coord[1]=-(-1f+y*2/height);
        return coord;
    }
    public boolean onTouch(View v, MotionEvent event) {
        float x=event.getX();
        float y=event.getY();
        int eventMask=event.getActionMasked();
        glSurfaceView.queueEvent(new SyncOnTouch(x,y,eventMask));
        return true;
    }
    public void setNewKub(final int n){
        kubChangeListener.kubChanged(n);
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                kub=new Kub(n);
                kub.shaderProgramChanged(shaderProgram);
            }
        });
    }
    public void setNewKub(final int[][][] grani){
        kubChangeListener.kubChanged(grani[0].length);
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                kub=new Kub(grani);
                kub.shaderProgramChanged(shaderProgram);
            }
        });
    }
    public void shuffle(){
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                kub.shuffle();
            }
        });
    }

    public void solve(){
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                com.example.dim.opengl.kub.State state=kub.getState();
                if(state.getN()!=3)return;
                if(state.isRotating())return;
                int[][][] grani=new int[6][3][3];
                state.getKvColor(grani);
                grani=FormatConverter.normalizeGrani(grani);
                try {
                    long st=System.currentTimeMillis();
                    Solution solution=new KubSolver().solve(new kub.kubSolver.Kub(grani),null,1);
                    Log.i(TAG,"Solution= "+solution);
                    Log.i(TAG,"Solution time= "+(System.currentTimeMillis()-st)+" ms");

                    kub.setPoslPovorots(FormatConverter.convertHods(solution.getHods()));
                } catch (InvalidPositionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class SyncOnTouch implements Runnable{
        private final float x;
        private final float y;
        private final int eventMask;
        private SyncOnTouch(float x,float y,int eventMask) {
            this.x=x;
            this.y=y;
            this.eventMask=eventMask;
        }
        public void run() {
            float[] coord = toOpenglOutCoord(x, y);
            kub.onTouch(coord, viewMatrix, monitorMatrix, eventMask);
        }
    }
    public static class State implements Serializable{
        private final com.example.dim.opengl.kub.State kubState;
        private final float[] viewMatrix;
        public State(com.example.dim.opengl.kub.State kubState, float[] viewMatrix) {
            this.kubState = kubState;
            this.viewMatrix = viewMatrix;
        }
        public com.example.dim.opengl.kub.State getKubState(){
            return new com.example.dim.opengl.kub.State(kubState);
        }
    }
}
