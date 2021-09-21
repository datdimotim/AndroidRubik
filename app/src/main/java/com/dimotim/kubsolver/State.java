package com.dimotim.kubsolver;

import java.io.Serializable;

import lombok.Value;

@Value
public class State implements Serializable {
    com.dimotim.kubsolver.kub.State kubState;
    float[] viewMatrix;
}
