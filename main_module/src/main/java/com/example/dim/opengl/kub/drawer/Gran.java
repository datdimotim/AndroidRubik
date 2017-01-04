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
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

class Gran {
    private final int n;
    private final int indexId;
    private final FloatBuffer vertexData;
    private final FloatBuffer texData;

    Gran(int n) {
        this.n = n;
        ShortBuffer indexData = ByteBuffer.allocateDirect(n * n * 2 * 3 * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        vertexData = ByteBuffer.allocateDirect(n * n * 4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texData = ByteBuffer.allocateDirect(n * n * 4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Kvadratik kvadratik = new Kvadratik(i, j);
                indexData.put(kvadratik.ind);
                vertexData.put(kvadratik.vert);
                texData.put(kvadratik.tex);
            }
        }
        indexData.position(0);
        vertexData.position(0);
        texData.position(0);
        int[] a = new int[1];
        glGenBuffers(1, a, 0);
        indexId = a[0];
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexData.capacity() * 2, indexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    void draw(SimpleShaderProgram program, FloatBuffer colorData, int srez, boolean in) {
        glVertexAttribPointer(program.textureArrayId, 2, GL_FLOAT, false, 0, texData);
        glVertexAttribPointer(program.colorArrayId, 3, GL_FLOAT, false, 0, colorData);
        glVertexAttribPointer(program.vertexArrayId, 2, GL_FLOAT, false, 0, vertexData);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexId);
        if (srez != -1) {
            if (in) {
                int firstSkip = (srez - 1) * n;
                int lastSkip = (n - srez) * n;
                glDrawElements(GL_TRIANGLES, 6 * n * n - 6 * (firstSkip + lastSkip), GL_UNSIGNED_SHORT, 6 * 2 * firstSkip);
            } else {
                int firstSkip = 0;
                int lastSkip = (n - srez + 1) * n;
                glDrawElements(GL_TRIANGLES, 6 * n * n - 6 * (firstSkip + lastSkip), GL_UNSIGNED_SHORT, 6 * 2 * firstSkip);
                firstSkip = (srez) * n;
                lastSkip = 0;
                glDrawElements(GL_TRIANGLES, 6 * n * n - 6 * (firstSkip + lastSkip), GL_UNSIGNED_SHORT, 6 * 2 * firstSkip);
            }
        } else {
            int firstSkip = 0;
            int lastSkip = 0;
            glDrawElements(GL_TRIANGLES, 6 * n * n - 6 * (firstSkip + lastSkip), GL_UNSIGNED_SHORT, 6 * 2 * firstSkip);
        }
    }

    protected void finalize() throws Throwable {
        super.finalize();
        glDeleteBuffers(1, new int[]{indexId}, 0);
    }

    private class Kvadratik {
        final float[] tex;
        final float[] vert;
        final short[] ind;

        Kvadratik(int i, int j) {
            short offset = (short) (j * 4 + i * n * 4);
            float a = (float) 2 / n;
            float dx = -1 + (float) 2 / n * j;
            float dy = -1 + (float) 2 / n * i;
            tex = new float[]{0, 0,
                    0, 1,
                    1, 1,
                    1, 0
            };
            vert = new float[]{dx, dy,
                    dx, dy + a,
                    dx + a, dy + a,
                    dx + a, dy
            };
            ind = new short[]{offset, (short) (1 + offset), (short) (2 + offset),
                    offset, (short) (2 + offset), (short) (3 + offset)
            };
        }
    }
}
