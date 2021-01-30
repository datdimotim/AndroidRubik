package com.dimotim.kubsolver.kub.drawer;
import android.opengl.Matrix;

import com.dimotim.kubsolver.SimpleShaderProgram;
import com.dimotim.kubsolver.kub.MatrixInitializer;
import com.dimotim.kubsolver.kub.PovorotInf;
import com.dimotim.kubsolver.kub.State;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glUniformMatrix4fv;

public class KubDrawer{
    private final float[][] matrix= MatrixInitializer.initMatrix();
    private final int n;

    private Object kvHash;
    private final FloatBuffer[] colorData=new FloatBuffer[6];
    private final FloatBuffer[] colorDataR=new FloatBuffer[6];
    private static final float[][] COLORS={ {0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f},
                                            {1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f},
                                            {1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f},
                                            {0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f},
                                            {0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,0.0f,1.0f},
                                            {1.0f,1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,1.0f,0.0f},
                                            {1.0f,0.5f,0.0f,1.0f,0.5f,0.0f,1.0f,0.5f,0.0f,1.0f,0.5f,0.0f}};

    private SimpleShaderProgram program;
    private Gran gran;
    private Zaglushka zaglushka;
    public void setProgram(SimpleShaderProgram program){
        this.program=program;
        gran=new Gran(n);
        zaglushka=new Zaglushka();
    }
    public KubDrawer(int n){
        this.n=n;
        for(int i=0;i<6;i++){
            colorData[i] = ByteBuffer.allocateDirect(n * n * 4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            colorDataR[i] = ByteBuffer.allocateDirect(n * n * 4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
    }

    private void changeColors(int[][][] colors){
        final int n=colors[0].length;
        for (int gran=0;gran<6;gran++) {
            int pos=0;
            float[] m=new float[4*3*n*n];
            float[] mR=new float[4*3*n*n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    System.arraycopy(COLORS[colors[gran][i][j]],0,m,pos,12);
                    System.arraycopy(COLORS[colors[gran][n-j-1][i]],0,mR,pos,12);
                    pos+=12;
                }
            }
            colorData[gran].put(m);
            colorDataR[gran].put(mR);
            colorData[gran].position(0);
            colorDataR[gran].position(0);
        }
    }

    public void draw(State state){
        if(kvHash!=state.getHash()){
            kvHash=state.getHash();
            int[][][] kvColors=new int[6][n][n];
            state.getKvColor(kvColors);
            changeColors(kvColors);
        }
        final int modelMatrixId=program.modelMatrixId;
        if(!state.isRotating()){
            for (int i=0;i<6;i++){
                glUniformMatrix4fv(modelMatrixId, 1, false, matrix[i], 0);
                gran.draw(program,colorData[i], -1, false);
            }
        }
        else{
            final float ugol=state.getUgol();
            PovorotInf current=state.getCurrentPovorot();
            final int storona=current.getStorona();
            final int srez=current.getSrez();
            switch (storona){
                case 1:{
                    if(srez==1) {
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[0], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, -1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[0], -1, false);
                    }
                    else{
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[0], 0);
                        gran.draw(program,colorData[0], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0, -1 + 2f / n * (srez - 1), 0);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, -1.0f, 0.0f);
                        Matrix.rotateM(matRot, 0, 90, 1, 0, 0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0, -1 + 2f / n * (srez - 1), 0);
                        Matrix.rotateM(matRot, 0, 90, 1, 0, 0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }
                    if(srez==n){
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[5], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, -1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[5], -1, false);
                    }
                    else {
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[5], 0);
                        gran.draw(program,colorData[5], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0, -1 + 2f / n * (srez), 0);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, -1.0f, 0.0f);
                        Matrix.rotateM(matRot, 0, 90, 1, 0, 0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0, -1 + 2f / n * (srez), 0);
                        Matrix.rotateM(matRot,0,90,1,0,0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }

                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[1], 0);
                    gran.draw(program,colorData[1], n - srez + 1, false);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[2], 0);
                    gran.draw(program,colorData[2], n - srez + 1, false);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[3], 0);
                    gran.draw(program,colorData[3], n - srez + 1, false);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[4], 0);
                    gran.draw(program,colorData[4], n - srez + 1, false);

                    float[] matRot=new float[16];
                    System.arraycopy(matrix[1], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[1], n - srez + 1, true);

                    System.arraycopy(matrix[4], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[4], n - srez + 1, true);

                    System.arraycopy(matrix[2], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[2], n - srez + 1, true);

                    System.arraycopy(matrix[3], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[3], n - srez + 1, true);
                    break;
                }
                case 2:{
                    if(srez==1) {
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[1], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, 1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[1], -1, false);
                    }
                    else{
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[1], 0);
                        gran.draw(program,colorData[1], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0, 0, 1 - 2f / n * (srez - 1));
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, 1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0,0, 1 - 2f / n * (srez - 1));
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }
                    if(srez==n){
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[4], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, 1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[4], -1, false);
                    }
                    else {
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[4], 0);
                        gran.draw(program,colorData[4], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0,0, 1 - 2f / n * (srez));
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, 1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 0,0, 1 - 2f / n * (srez));
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }
                    float[] mat90=new float[16];

                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[5], 0);
                    gran.draw(program,colorData[5], n - srez + 1, false);
                    System.arraycopy(matrix[2], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[2], srez, false);
                    System.arraycopy(matrix[3], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[3], srez, false);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matrix[0], 0);
                    gran.draw(program,colorData[0], n - srez + 1, false);

                    float[] matRot=new float[16];
                    System.arraycopy(matrix[0], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[0], n - srez + 1, true);

                    System.arraycopy(matrix[5], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, 0.0f, 1.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorData[5], n - srez + 1, true);

                    System.arraycopy(matrix[2], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[2], srez, true);

                    System.arraycopy(matrix[3], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[3], srez, true);
                    break;
                }
                case 3:{
                    if(srez==1) {
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[2], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, -1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[2], -1, false);
                    }
                    else{
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[2], 0);
                        gran.draw(program,colorData[2], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 1 - 2f / n * (srez - 1), 0, 0);
                        Matrix.rotateM(matRot, 0, ugol, 1.0f, 0.0f, 0.0f);
                        Matrix.rotateM(matRot, 0, 90, 0, 1, 0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0,  1 - 2f / n * (srez - 1),0, 0);
                        Matrix.rotateM(matRot,0,90,0,1,0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }
                    if(srez==n){
                        float[] matRot=new float[16];
                        System.arraycopy(matrix[3], 0, matRot, 0, 16);
                        Matrix.rotateM(matRot, 0, ugol, 0.0f, 0.0f, -1.0f);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        gran.draw(program,colorData[3], -1, false);
                    }
                    else {
                        glUniformMatrix4fv(modelMatrixId, 1, false, matrix[3], 0);
                        gran.draw(program,colorData[3], -1, false);

                        float[] matRot=new float[16];
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0, 1 - 2f / n * (srez), 0, 0);
                        Matrix.rotateM(matRot, 0, ugol, 1.0f, 0.0f, 0.0f);
                        Matrix.rotateM(matRot, 0, 90, 0, 1, 0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                        Matrix.setIdentityM(matRot, 0);
                        Matrix.translateM(matRot, 0,  1 - 2f / n * (srez ),0, 0);
                        Matrix.rotateM(matRot,0,90,0,1,0);
                        glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                        zaglushka.draw(program);
                    }
                    float[] mat90=new float[16];
                    System.arraycopy(matrix[0], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[0], srez, false);

                    System.arraycopy(matrix[5], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[5], srez, false);

                    System.arraycopy(matrix[1], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[1], srez, false);

                    System.arraycopy(matrix[4], 0, mat90, 0, 16);
                    Matrix.rotateM(mat90, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, mat90, 0);
                    gran.draw(program,colorDataR[4], srez, false);


                    float[] matRot=new float[16];
                    System.arraycopy(matrix[0], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[0], srez, true);

                    System.arraycopy(matrix[5], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[5], srez, true);

                    System.arraycopy(matrix[1], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[1], srez, true);

                    System.arraycopy(matrix[4], 0, matRot, 0, 16);
                    Matrix.translateM(matRot, 0, 0, 0, 1);
                    Matrix.rotateM(matRot, 0, ugol, -1.0f, 0.0f, 0.0f);
                    Matrix.translateM(matRot, 0, 0, 0, -1);
                    Matrix.rotateM(matRot, 0, -90, 0, 0, 1);
                    glUniformMatrix4fv(modelMatrixId, 1, false, matRot, 0);
                    gran.draw(program,colorDataR[4], srez, true);
                    break;
                }
                default: throw new RuntimeException("incorrect storona: storona="+storona);
            }
        }

    }
}
