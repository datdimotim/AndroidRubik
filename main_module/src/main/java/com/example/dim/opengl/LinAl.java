package com.example.dim.opengl;

import android.opengl.Matrix;

public  class LinAl{
    public static float[] vecUmnVec(float[] a, float[] b){
        int x=0,y=1,z=2;
        float[] res=new float[3];
        res[x]=a[y]*b[z]-a[z]*b[y];
        res[y]=a[z]*b[x]-a[x]*b[z];
        res[z]=a[x]*b[y]-a[y]*b[x];
        return res;
    }
    public static float saclarMul(float[] a, float[] b){
        return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];
    }
    public static float dlina(float[] a){
        return  (float)Math.pow(a[0]*a[0]+a[1]*a[1]+a[2]*a[2],0.5);
    }
    public static float[] normalize(float[] a){
        float[] b=new float[3];
        float dlin=dlina(a);
        for(int i=0;i<3;i++){
            b[i]=a[i]/dlin;
        }
        return b;
    }
    public static float[] sum(float[] a, float[] b){
        float[] c=new float[a.length];
        for(int i=0;i<a.length;i++){
            c[i]=a[i]+b[i];
        }
        return c;
    }
    public static float[] razn(float[] a, float[] b){
        float[] c=new float[a.length];
        for(int i=0;i<a.length;i++){
            c[i]=a[i]-b[i];
        }
        return c;
    }
    public static float[][] minor(float[][] m,int x,int y){
        float[][] min=new float[m.length-1][m[0].length-1];
        for (int j=0;j<m.length;j++){
            if(j==y)continue;
            for(int i=0;i<m[0].length;i++){
                if(i==x)continue;
                if(i<x){
                    if(j<y)min[i][j]=m[i][j];
                    else min[i][j-1]=m[i][j];
                }
                else {
                    if(j<y)min[i-1][j]=m[i][j];
                    else min[i-1][j-1]=m[i][j];
                }
            }
        }
        return min;
    }
    public static float det(float[][] m){
        if(m.length==1)return m[0][0];
        float det=0;
        for (int i=0;i<m.length;i++){
            int s;
            if(i==i/2*2)s=1;
            else  s=-1;
            det=det+s*m[i][0]*det(minor(m,i,0));
        }
        return det;
    }
    public static float[] toLinear(float[][] matrix){
        float[] lin=new float[matrix.length*matrix[0].length];
        for (int i=0;i<matrix.length;i++){
            System.arraycopy(matrix[i], 0, lin, i * matrix[0].length, matrix.length);
        }
        return lin;
    }
    public static float[][] toQuad(float[] lin,int n){
        float[][] matrix=new float[n][n];
        for (int i=0;i<matrix.length;i++){
            System.arraycopy(lin, i * matrix[0].length, matrix[i], 0, matrix.length);
        }
        return matrix;
    }
    public static float[][] inverse(float[][] mat){
        float[][] inv=new float[mat.length][mat.length];
        float det=det(mat);
        for(int i=0;i<mat.length;i++){
            for (int j=0;j<mat.length;j++){
                int s;
                if(i+j==(i+j)/2*2)s=1;
                else  s=-1;
                inv[i][j]=s*det(minor(mat, i, j))/det;
            }
        }
        float[] lin=new float[mat.length*mat[0].length];
        Matrix.transposeM(lin,0,toLinear(inv),0);
        return toQuad(lin,mat.length);
    }
}
