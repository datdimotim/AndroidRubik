package com.dimotim.kubsolver.kub;

import com.dimotim.kubsolver.kub.facelet.Povorot;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

public class State implements Serializable {
    final int n;
    private final int[][][] kvColor;
    private transient Object kvHash=new Object();

    private final LinkedList<PovorotInf> povorots=new LinkedList<>();
    float ugol;

    State(int[][][] kvColor, int n, float ugol,LinkedList<PovorotInf> povorots) {
        this.kvColor = new int[6][n][n];
        for (int g = 0; g < 6; g++) {
            for (int i = 0; i < n; i++) {
                System.arraycopy(kvColor[g][i], 0, this.kvColor[g][i], 0, n);
            }
        }
        this.ugol = ugol;
        this.n = n;

        for (PovorotInf p:povorots)this.povorots.add(p);
    }

    public State(State state) {
        this(state.kvColor, state.n, state.ugol,state.povorots);
    }
    State(int n) {
        ugol = 0;
        this.n=n;
        kvColor = new int[6][n][n];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    kvColor[i][j][k] = i + 1;
                }
            }
        }
    }
    State(int[][][] grani){
        ugol=0;
        this.n=grani[0].length;
        kvColor=new int[6][n][n];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < n; j++) {
                System.arraycopy(grani[i][j], 0, kvColor[i][j], 0, n);
            }
        }
    }

    public float getUgol() {
        return ugol;
    }

    public int getN() {
        return n;
    }

    public void getKvColor(int[][][] ret) {
        for(int i=0;i<6;i++)for(int j=0;j<n;j++) System.arraycopy(kvColor[i][j], 0, ret[i][j], 0, n);
    }
    void setKvColor(int[][][] kvColor){
        for(int i=0;i<6;i++)for(int j=0;j<n;j++) System.arraycopy(kvColor[i][j], 0, this.kvColor[i][j], 0, n);
        kvHash=new Object();
        ugol = 0;
    }
    public Object getHash(){
        return kvHash;
    }

    void nextPovorot(){
        if(povorots.isEmpty())return;
        Povorot.rotate(kvColor, povorots.peekFirst().storona,
                                povorots.peekFirst().srez,
                                povorots.peekFirst().sign);
        kvHash=new Object();
        ugol = 0;
        povorots.poll();
    }
    public boolean isRotating(){
        return !povorots.isEmpty();
    }
    public PovorotInf getCurrentPovorot(){return povorots.peekFirst();}
    void addPovorotsInQueue(PovorotInf[] posl){
        Collections.addAll(povorots, posl);
    }
    void resetPovorots(){
        povorots.clear();
    }
}

