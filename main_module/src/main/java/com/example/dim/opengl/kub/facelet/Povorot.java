package com.example.dim.opengl.kub.facelet;

public class Povorot {
    public static void rotate(int[][][] kvColor,int storona,int srez,int sign){
        final int n=kvColor[0].length;
        int[][][] kvCopy=new int[6][n][n];
        for(int i=0;i<6;i++){
            for(int j=0;j<n;j++){
                System.arraycopy(kvColor[i][j], 0, kvCopy[i][j], 0, n);
            }
        }
        switch (storona){
            case 1:{
                if(sign>0) {
                    for (int i = 0; i < n; i++) {
                        kvColor[1][n - srez][i] = kvCopy[2][n - srez][n - i - 1];
                        kvColor[3][n - srez][i] = kvCopy[1][n - srez][i];
                        kvColor[2][n - srez][i] = kvCopy[4][n - srez][i];
                        kvColor[4][n - srez][i] = kvCopy[3][n - srez][n - i - 1];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[0]);
                    }
                    if (srez == n) {
                        rotateColors90(kvColor[5]);
                    }
                }
                else{
                    for (int i = 0; i < n; i++) {
                        kvColor[2][n - srez][i] = kvCopy[1][n - srez][n - i - 1];
                        kvColor[1][n - srez][i] = kvCopy[3][n - srez][i];
                        kvColor[4][n - srez][i] = kvCopy[2][n - srez][i];
                        kvColor[3][n - srez][i] = kvCopy[4][n - srez][n - i - 1];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[0]);
                        rotateColors90(kvColor[0]);
                        rotateColors90(kvColor[0]);
                    }
                    if (srez == n) {
                        rotateColors90(kvColor[5]);
                        rotateColors90(kvColor[5]);
                        rotateColors90(kvColor[5]);
                    }
                }
                break;
            }
            case 3:{
                if(sign<0) {
                    for (int i = 0; i < n; i++) {
                        kvColor[5][i][srez-1] = kvCopy[1][i][srez-1];
                        kvColor[1][i][srez-1] = kvCopy[0][n- i - 1][srez-1];
                        kvColor[0][i][srez-1] = kvCopy[4][i][srez-1];
                        kvColor[4][i][srez-1] = kvCopy[5][n - i - 1][srez-1];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[2]);
                        rotateColors90(kvColor[2]);
                        rotateColors90(kvColor[2]);
                    }
                    if(srez==n){
                        rotateColors90(kvColor[3]);
                        rotateColors90(kvColor[3]);
                        rotateColors90(kvColor[3]);
                    }
                }
                else{
                    for (int i = 0; i < n; i++) {
                        kvColor[1][i][srez-1] = kvCopy[5][i][srez-1];
                        kvColor[0][i][srez-1] = kvCopy[1][n- i - 1][srez-1];
                        kvColor[4][i][srez-1] = kvCopy[0][i][srez-1];
                        kvColor[5][i][srez-1] = kvCopy[4][n - i - 1][srez-1];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[2]);
                    }
                    if(srez==n){
                        rotateColors90(kvColor[3]);
                    }
                }
                break;
            }
            case 2:{
                if(sign>0) {
                    for (int i = 0; i < n; i++) {
                        kvColor[5][n-srez][n-i-1] = kvCopy[2][i][srez-1];
                        kvColor[2][i][srez-1] = kvCopy[0][n-srez][i];
                        kvColor[0][n-srez] [n-i-1]= kvCopy[3][i][srez-1];
                        kvColor[3][i][srez-1] = kvCopy[5][n-srez][i];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[1]);
                        rotateColors90(kvColor[1]);
                        rotateColors90(kvColor[1]);
                    }
                    if(srez==n){
                        rotateColors90(kvColor[4]);
                        rotateColors90(kvColor[4]);
                        rotateColors90(kvColor[4]);
                    }
                }
                else{
                    for (int i = 0; i < n; i++) {
                        kvColor[2][n-i-1][srez-1] = kvCopy[5][n-srez][i];
                        kvColor[0][n-srez][i] = kvCopy[2][i][srez-1];
                        kvColor[3][n-i-1][srez-1]= kvCopy[0][n-srez][i];
                        kvColor[5][n-srez][i] = kvCopy[3][i][srez-1];
                    }
                    if(srez==1){
                        rotateColors90(kvColor[1]);
                    }
                    if(srez==n){
                        rotateColors90(kvColor[4]);
                    }
                }
                break;
            }
        }
    }
    private static void rotateColors90(int[][] kv){
        final int n=kv.length;
        int[][] kvCopy=new int[n][n];
        for(int j=0;j<n;j++){
            System.arraycopy(kv[j], 0, kvCopy[j], 0, n);
        }
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                kv[i][j]=kvCopy[j][n-1-i];
            }
        }
    }
}
