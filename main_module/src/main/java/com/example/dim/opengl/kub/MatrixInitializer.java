package com.example.dim.opengl.kub;

import android.opengl.Matrix;

public class MatrixInitializer {
    public static float[][] initMatrix(){
        float[][] matrix=new float[6][16];
        Matrix.setIdentityM(matrix[0], 0);
        Matrix.translateM(matrix[0], 0, 0.0f, -1.0f, 0.0f);
        Matrix.rotateM(matrix[0], 0, 90, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(matrix[0], 0, 180, 0.0f, 1.0f, 0.0f);
        Matrix.setIdentityM(matrix[5], 0);
        Matrix.translateM(matrix[5], 0, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(matrix[5], 0, 90, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(matrix[5], 0, 180, 0.0f, 1.0f, 0.0f);
        Matrix.setIdentityM(matrix[1], 0);
        Matrix.translateM(matrix[1], 0, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(matrix[1], 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.setIdentityM(matrix[4], 0);
        Matrix.translateM(matrix[4], 0, 0.0f, 0.0f, -1.0f);
        Matrix.rotateM(matrix[4], 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.setIdentityM(matrix[2], 0);
        Matrix.translateM(matrix[2], 0, 1.0f, 0.0f, 0.0f);
        Matrix.rotateM(matrix[2], 0, 90, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(matrix[2], 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(matrix[2], 0, 180, 0.0f, 1.0f, 0.0f);
        Matrix.setIdentityM(matrix[3], 0);
        Matrix.translateM(matrix[3], 0, -1.0f, 0.0f, 0.0f);
        Matrix.rotateM(matrix[3], 0, 90, 0.0f, 1.0f, 0.0f);
        Matrix.rotateM(matrix[3], 0, 180, 0.0f, 0.0f, 1.0f);
        Matrix.rotateM(matrix[3],0,180,0.0f,1.0f,0.0f);
        return matrix;
    }
}
