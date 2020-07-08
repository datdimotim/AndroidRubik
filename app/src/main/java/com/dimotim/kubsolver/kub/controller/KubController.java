package com.dimotim.kubsolver.kub.controller;

import android.opengl.Matrix;
import android.view.MotionEvent;

import com.dimotim.kubsolver.kub.Kub;
import com.dimotim.kubsolver.kub.MatrixInitializer;
import com.dimotim.kubsolver.kub.Povorotable;


public class KubController {
    private final float[][] matrix= MatrixInitializer.initMatrix();
    private final int n;
    private final Povorotable kub;

    private int startGran;
    private final float[] coordStart = new float[2];
    private final float[] startMatrix = new float[16];
    private boolean inKub = false;

    public KubController(int n, Kub kub) {
        this.n = n;
        this.kub = kub;
    }

    public void command(float[] matrix, float[] monitorMatrix, Kub.TouchEvent event) {
        float[] coord={event.getX(),event.getY()};
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                float zRec = 2f;
                int gran = -1;
                for (int ig = 0; ig < 6; ig++) {
                    float[] coordsInGran = getCoordsInGran(ig, coord, matrix, monitorMatrix);
                    float x = coordsInGran[0];
                    float y = coordsInGran[1];
                    float z = coordsInGran[2];
                    if (Math.abs(x) < 1f & Math.abs(y) < 1f) {
                        if (z < zRec) {
                            zRec = z;
                            gran = ig;
                        }
                    }
                }
                if (gran != -1) {
                    inKub = true;
                    startGran = gran;
                    this.coordStart[0] = coord[0];
                    this.coordStart[1] = coord[1];
                } else {
                    this.coordStart[0] = coord[0];
                    this.coordStart[1] = coord[1];
                    inKub = false;
                    System.arraycopy(matrix, 0, startMatrix, 0, 16);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (inKub) return;
                else {
                    if (!(coordStart[0] == coord[0] & coordStart[1] == coord[1])) {
                        float[] mat = new float[16];
                        Matrix.setIdentityM(mat, 0);
                        Matrix.rotateM(mat, 0, 100f * (float) Math.pow((coordStart[0] - coord[0]) * (coordStart[0] - coord[0]) + (coordStart[1] - coord[1]) * (coordStart[1] - coord[1]), 0.5)
                                , coord[1] - coordStart[1], -(coord[0] - coordStart[0]), 0);
                        Matrix.multiplyMM(matrix, 0, mat, 0, startMatrix, 0);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (!inKub) {
                    inKub = false;
                    return;
                } else {
                    inKub = false;
                    Rotating rotating = getRotating(matrix, monitorMatrix, coord);
                    switch (rotating.gran) {
                        case 0: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(2, n - rotating.j, 1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(3, rotating.i + 1, -1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(2, n - rotating.j, -1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(3, rotating.i + 1, 1);
                                    return;
                                }
                            }
                            break;
                        }
                        case 5: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(2, n - rotating.j, -1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(3, rotating.i + 1, 1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(2, n - rotating.j, 1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(3, rotating.i + 1, -1);
                                    return;
                                }
                            }
                            break;
                        }
                        case 1: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(1, n - rotating.j, -1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(3, rotating.i + 1, 1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(1, n - rotating.j, 1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(3, rotating.i + 1, -1);
                                    return;
                                }
                            }
                            break;
                        }
                        case 4: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(1, n - rotating.j, 1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(3, rotating.i + 1, -1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(1, n - rotating.j, -1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(3, rotating.i + 1, 1);
                                    return;
                                }
                            }
                            break;
                        }
                        case 2: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(1, n - rotating.j, 1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(2, rotating.i + 1, -1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(1, n - rotating.j, -1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(2, rotating.i + 1, 1);
                                    return;
                                }
                            }
                            break;
                        }
                        case 3: {
                            switch (rotating.napr) {
                                case 1: {
                                    kub.povorot(1, n - rotating.j, -1);
                                    return;
                                }
                                case 2: {
                                    kub.povorot(2, rotating.i + 1, 1);
                                    return;
                                }
                                case 3: {
                                    kub.povorot(1, n - rotating.j, 1);
                                    return;
                                }
                                case 4: {
                                    kub.povorot(2, rotating.i + 1, -1);
                                    return;
                                }
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private Rotating getRotating(float[] matrix, float[] monitorMatrix, float[] coord) {
        float[] coord1 = getCoordsInGran(startGran, this.coordStart, matrix, monitorMatrix);
        float[] coord2 = getCoordsInGran(startGran, coord, matrix, monitorMatrix);
        int[] ind = xy2LineCol(coord1[0], coord1[1]);
        Point2d p1 = new Point2d(-1f + 2f * (ind[1]) / n, -1f + 2f * (ind[0]) / n);
        Point2d p2 = new Point2d(-1f + 2f * (ind[1]) / n, -1f + 2f * (ind[0] + 1) / n);
        Point2d p3 = new Point2d(-1f + 2f * (ind[1] + 1) / n, -1f + 2f * (ind[0] + 1) / n);
        Point2d p4 = new Point2d(-1f + 2f * (ind[1] + 1) / n, -1f + 2f * (ind[0]) / n);

        int napr = 0;
        if (peresek(p1, p2, new Point2d(coord1), new Point2d(coord2))) napr = 1;
        if (peresek(p2, p3, new Point2d(coord1), new Point2d(coord2))) napr = 2;
        if (peresek(p3, p4, new Point2d(coord1), new Point2d(coord2))) napr = 3;
        if (peresek(p4, p1, new Point2d(coord1), new Point2d(coord2))) napr = 4;
        return new Rotating(startGran, ind[1], ind[0], napr);
    }

    private float[] getCoordsInGran(int ig, float[] coord, float[] matrix, float[] monitorMatrix) {
        float[] globalMatrix = new float[16];
        float[] tmp = new float[16];
        Matrix.multiplyMM(tmp, 0, matrix, 0, this.matrix[ig], 0);
        Matrix.multiplyMM(globalMatrix, 0, monitorMatrix, 0, tmp, 0);
        float[] inverseMatrix = new float[16];
        Matrix.invertM(inverseMatrix,0,globalMatrix,0);
        float[] p1 = {coord[0], coord[1], -1.0f, 1.0f};
        float[] p2 = {coord[0], coord[1], 1.0f, 1.0f};
        float[] p1R = new float[4];
        float[] p2R = new float[4];

        Matrix.multiplyMV(p1R, 0, inverseMatrix, 0, p1, 0);
        Matrix.multiplyMV(p2R, 0, inverseMatrix, 0, p2, 0);

        p1R[0]=p1R[0]/p1R[3];
        p1R[1]=p1R[1]/p1R[3];
        p1R[2]=p1R[2]/p1R[3];

        p2R[0]=p2R[0]/p2R[3];
        p2R[1]=p2R[1]/p2R[3];
        p2R[2]=p2R[2]/p2R[3];

        float y = p1R[2] * (p1R[1] - p2R[1]) / (p2R[2] - p1R[2]) + p1R[1];
        float x = p1R[2] * (p1R[0] - p2R[0]) / (p2R[2] - p1R[2]) + p1R[0];
        float[] pEkr = new float[4];
        Matrix.multiplyMV(pEkr, 0, globalMatrix, 0, new float[]{x, y, 0, 1}, 0);
        return new float[]{x, y, pEkr[2]/pEkr[3]};
    }

    private int[] xy2LineCol(float x, float y) {
        int iLine = 0;
        for (int i = 0; i < n; i++) {
            float yLine = -1f + 2f * (i + 1) / n;
            if (yLine > y) {
                iLine = i;
                break;
            }
        }
        int iCol = 0;
        for (int i = 0; i < n; i++) {
            float xCol = -1f + 2f * (i + 1) / n;
            if (xCol > x) {
                iCol = i;
                break;
            }
        }
        return new int[]{iLine, iCol};
    }

    private boolean peresek(Point2d a, Point2d b, Point2d c, Point2d d) {
        if (a.x == b.x) {
            if ((c.x - a.x) * (d.x - a.x) >= 0) return false;
        } else {
            if (!(((a.y - b.y) / (a.x - b.x) * c.x + (a.x * b.y - a.y * b.x) / (a.x - b.x) > c.y) & ((a.y - b.y) / (a.x - b.x) * d.x + (a.x * b.y - a.y * b.x) / (a.x - b.x) < d.y) ||
                    ((a.y - b.y) / (a.x - b.x) * c.x + (a.x * b.y - a.y * b.x) / (a.x - b.x) < c.y) & ((a.y - b.y) / (a.x - b.x) * d.x + (a.x * b.y - a.y * b.x) / (a.x - b.x) > d.y)))
                return false;
        }
        if (c.x == d.x) {
            if ((a.x - c.x) * (b.x - c.x) >= 0) return false;
        } else {
            if (!(((c.y - d.y) / (c.x - d.x) * a.x + (c.x * d.y - c.y * d.x) / (c.x - d.x) > a.y) & ((c.y - d.y) / (c.x - d.x) * b.x + (c.x * d.y - c.y * d.x) / (c.x - d.x) < b.y) ||
                    ((c.y - d.y) / (c.x - d.x) * a.x + (c.x * d.y - c.y * d.x) / (c.x - d.x) < a.y) & ((c.y - d.y) / (c.x - d.x) * b.x + (c.x * d.y - c.y * d.x) / (c.x - d.x) > b.y)))
                return false;
        }
        return true;
    }

    private static class Point2d {
        float x, y;

        Point2d(float x, float y) {
            this.x = x;
            this.y = y;
        }

        Point2d(float[] m) {
            x = m[0];
            y = m[1];
        }
    }

    private static class Rotating {
        private final int gran, i, j, napr;

        Rotating(int gran, int i, int j, int napr) {
            this.gran = gran;
            this.i = i;
            this.j = j;
            this.napr = napr;
        }
    }
}
