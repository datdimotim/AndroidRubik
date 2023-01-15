package com.dimotim.kubsolver;

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
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.dimotim.kubSolver.Kub2x2;
import com.dimotim.kubSolver.Solution;
import com.dimotim.kubsolver.dialogs.SolveDialog;
import com.dimotim.kubsolver.kub.Kub;
import com.dimotim.kubsolver.kub.to_solver_interface.FormatConverter;
import com.dimotim.kubsolver.shaderUtils.TextureUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer, View.OnTouchListener {
    public static final String TAG = "renderer: ";
    private SimpleShaderProgram shaderProgram;
    private final float[] viewMatrix = new float[16];
    private final float[] monitorMatrix = new float[16];
    private float width;
    private float height;
    private final Bitmap bitmap;
    private final String fragmentShaderText;
    private final String vertexShaderText;
    private Kub kub;
    private final GLSurfaceView glSurfaceView;
    private final KubChangeListener kubChangeListener;
    private final Solvers solvers;

    public interface KubChangeListener {
        void kubChanged(int size);
    }

    public OpenGLRenderer(GLSurfaceView glSurfaceView,
                          Bitmap bitmap,
                          String vertexShaderText,
                          String fragmentShaderText,
                          State saveState,
                          KubChangeListener kubChangeListener,
                          Solvers solvers) {
        this.kubChangeListener = kubChangeListener;
        this.glSurfaceView = glSurfaceView;
        this.bitmap = bitmap;
        this.fragmentShaderText = fragmentShaderText;
        this.vertexShaderText = vertexShaderText;
        this.solvers=solvers;
        Matrix.setIdentityM(viewMatrix, 0);

        if (saveState != null) {
            kub = new Kub(saveState.getKubState());
            kubChangeListener.kubChanged(saveState.getKubState().getN());
            System.arraycopy(saveState.getViewMatrix(), 0, viewMatrix, 0, 16);
        } else {
            kub = new Kub(3);
            kubChangeListener.kubChanged(3);
        }
    }

    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        Log.i(TAG, "onSurface created");
        glClearColor(0.7f, 0.7f, 0.7f, 1f);
        glEnable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0);
        shaderProgram = new SimpleShaderProgram(vertexShaderText, fragmentShaderText);
        glUseProgram(shaderProgram.shaderProgramId);
        int textureImageId = TextureUtils.loadTextureFromBitmap(bitmap);
        kub.shaderProgramChanged(shaderProgram);

        glBindTexture(GL_TEXTURE_2D, textureImageId);
        glUniform1i(shaderProgram.textureId, 0);// 0- индекс текстурного блока
    }

    /**
     *
     * @param alpha угол обзора, радианы
     * @param aspect соотношение сторон экрана width/height
     * @return перспективная матрица
     */
    public float[] getPerspectiveMatrix(float alpha, float aspect) {
        // радиус описанной сферы вокруг кубика
        float R = (float) Math.sqrt(3);
        // двигаем центр кубика из 0 d, чтобы он отображался на весь экран
        float d = R / (float) sin(alpha/2);
        // ближняя плоскость отсечения
        float l = d - R;
        // дальняя плоскость отсечения
        float L = d + R;

        // Перспективная матрица, переводит координаты кубика в экранные
        // x: [-1, 1], y: [-1, 1], z: [0, 1]

        if(aspect < 1){
            return new float[]{
                    (float) (1 / tan(alpha/2)), 0, 0, 0,
                    0, (float) (aspect / (tan(alpha/2))), 0, 0,
                    0, 0, 1 / (L - l), 1,
                    0, 0, - l / (L - l), d
            };
        }else {
            return new float[]{
                    (float) (1 / aspect / tan(alpha/2)), 0, 0, 0,
                    0, (float) (1 / tan(alpha/2)), 0, 0,
                    0, 0, 1 / (L - l), 1,
                    0, 0, -l / (L - l), d
            };
        }
    }

    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        Log.i(TAG, "onSurface changed");
        this.width = width;
        this.height = height;

        float alpha = (float) (3.14 / 8.0);
        float aspect = width * 1.0f / height;
        float[] pers = getPerspectiveMatrix(alpha, aspect);
        System.arraycopy(pers, 0, monitorMatrix, 0, pers.length);
        glUniformMatrix4fv(shaderProgram.monitorMatrixId, 1, false, monitorMatrix, 0);
    }

    public void onDrawFrame(GL10 arg0) {
        glUniformMatrix4fv(shaderProgram.viewMatrixId, 1, false, viewMatrix, 0);
        glClear(GL_COLOR_BUFFER_BIT);
        glClear(GL_DEPTH_BUFFER_BIT);
        kub.draw();
    }

    public State getState() {
        final State[] state = new State[1];
        final Object sync = new Object();
        synchronized (sync) {
            glSurfaceView.queueEvent(() -> {
                float[] matSave = new float[16];
                System.arraycopy(viewMatrix, 0, matSave, 0, 16);
                com.dimotim.kubsolver.kub.State kubState = kub.getState();
                state[0] = new State(kubState, matSave);
                synchronized (sync) {
                    sync.notifyAll();
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

    public void setState(final State state) {
        kubChangeListener.kubChanged(state.getKubState().getN());
        glSurfaceView.queueEvent(() -> {
            kub = new Kub(state.getKubState());
            System.arraycopy(state.getViewMatrix(), 0, viewMatrix, 0, 16);
            kub.shaderProgramChanged(shaderProgram);
        });
    }

    private float[] toOpenglOutCoord(float x, float y) {
        float[] coord = new float[2];
        coord[0] = -1f + x * 2 / width;
        coord[1] = -(-1f + y * 2 / height);
        return coord;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event){
        int mask=event.getActionMasked();
        float x=event.getX();
        float y=event.getY();
        glSurfaceView.queueEvent(()-> {
            float[] normalizedCoords=toOpenglOutCoord(x,y);
            Kub.TouchEvent touchEvent=new Kub.TouchEvent(normalizedCoords[0],normalizedCoords[1],mask);
            kub.onTouch(viewMatrix,monitorMatrix,touchEvent);
        });
        return true;
    }

    public void setNewKub(final int n) {
        kubChangeListener.kubChanged(n);
        glSurfaceView.queueEvent(() -> {
            kub = new Kub(n);
            kub.shaderProgramChanged(shaderProgram);
        });
    }

    public void setNewKub(final int[][][] grani) {
        kubChangeListener.kubChanged(grani[0].length);
        glSurfaceView.queueEvent(() -> {
            kub = new Kub(grani);
            kub.shaderProgramChanged(shaderProgram);
        });
    }

    public void shuffle() {
        glSurfaceView.queueEvent(() -> kub.shuffle());
    }

    public void solve(SolveDialog.SolveEntry solveEntry) {
        glSurfaceView.queueEvent(() -> {
            com.dimotim.kubsolver.kub.State state = kub.getState();
            if (state.isRotating()) return;
            if (state.getN() == 3) {
                int[][][] grani = new int[6][3][3];
                state.getKvColor(grani);
                grani = FormatConverter.normalizeGrani(grani);
                try {
                    long st = System.currentTimeMillis();
                    com.dimotim.kubSolver.Kub uzor = new com.dimotim.kubSolver.Kub(false).apply(new Solution(solveEntry.getSolution().getHods()));
                    Solution solution = solvers.getUzorSolver().apply(new com.dimotim.kubSolver.Kub(grani), uzor);
                    Log.i(TAG, "Solution= " + solution);
                    Log.i(TAG, "Solution time= " + (System.currentTimeMillis() - st) + " ms");

                    kub.setPoslPovorots(FormatConverter.convertHods(solution.getHods(), 3));
                } catch (com.dimotim.kubSolver.Kub.InvalidPositionException e) {
                    e.printStackTrace();
                }
            }
            if (state.getN() == 2) {
                int[][][] grani = new int[6][2][2];
                state.getKvColor(grani);
                for (int i = 0; i < 6; i++)
                    for (int j = 0; j < 2; j++) for (int k = 0; k < 2; k++) grani[i][j][k]--;
                try {
                    long st = System.currentTimeMillis();
                    com.dimotim.kubSolver.Kub2x2 uzor = new com.dimotim.kubSolver.Kub2x2(false).apply(new Solution(solveEntry.getSolution().getHods()));
                    Solution solution = solvers.getUzor2x2Solver().apply(new Kub2x2(grani), uzor);
                    Log.i(TAG, "Solution= " + solution);
                    Log.i(TAG, "Solution time= " + (System.currentTimeMillis() - st) + " ms");

                    kub.setPoslPovorots(FormatConverter.convertHods(solution.getHods(), 2));
                } catch (Kub2x2.InvalidPositionException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
