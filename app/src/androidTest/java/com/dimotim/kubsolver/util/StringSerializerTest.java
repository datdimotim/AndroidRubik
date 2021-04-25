package com.dimotim.kubsolver.util;

import org.junit.Assert;
import org.junit.Test;

public class StringSerializerTest {
    @Test
    public void serializationTest() {
        int[] m = {1, 2, 3};
        int[] r = (int[]) StringSerializer.deserializeFromString(StringSerializer.serializeToString(m));
        Assert.assertArrayEquals(m, r);
    }
}