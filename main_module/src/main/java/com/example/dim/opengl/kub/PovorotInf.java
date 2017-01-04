package com.example.dim.opengl.kub;

import java.io.Serializable;

public class PovorotInf implements Serializable{
    public final int srez;
    public final int storona;
    public final int sign;
    public PovorotInf(int srez,int storona,int sign){
        this.srez=srez;this.sign=sign;this.storona=storona;
    }
}
