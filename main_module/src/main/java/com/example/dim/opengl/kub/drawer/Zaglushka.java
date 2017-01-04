package com.example.dim.opengl.kub.drawer;

import com.example.dim.opengl.SimpleShaderProgram;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

public class Zaglushka {
    private final int index;
    private final FloatBuffer vertexData;
    private final FloatBuffer texData;
    private final FloatBuffer colorData;

    public Zaglushka() {
        ShortBuffer indexData = ByteBuffer.allocateDirect(2 * 3 * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        vertexData = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texData = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        colorData = ByteBuffer.allocateDirect(4 * 3 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        colorData.put(new float[]{0.0f, 0.0f, 0.0f});
        colorData.put(new float[]{0.0f, 0.0f, 0.0f});
        colorData.put(new float[]{0.0f, 0.0f, 0.0f});
        colorData.put(new float[]{0.0f, 0.0f, 0.0f});
        colorData.position(0);

        texData.put(new float[]{0.0f, 0.0f});
        texData.put(new float[]{0.0f, 0.0f});
        texData.put(new float[]{0.0f, 0.0f});
        texData.put(new float[]{0.0f, 0.0f});
        texData.position(0);

        vertexData.put(new float[]{-1f, -1f,
                -1f, 1f,
                1f, 1f,
                1f, -1f});
        vertexData.position(0);
        indexData.put(new short[]{0, 1, 2, 0, 2, 3});
        indexData.position(0);

        int[] a = new int[1];
        glGenBuffers(1, a, 0);
        index = a[0];
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, index);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData.capacity() * 2, indexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void draw(SimpleShaderProgram program) {
        glVertexAttribPointer(program.textureArrayId, 2, GL_FLOAT, false, 0, texData);
        glVertexAttribPointer(program.colorArrayId, 3, GL_FLOAT, false, 0, colorData);
        glVertexAttribPointer(program.vertexArrayId, 2, GL_FLOAT, false, 0, vertexData);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, index);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, 0);
    }
}
