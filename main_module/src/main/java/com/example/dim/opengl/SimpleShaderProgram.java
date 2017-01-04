package com.example.dim.opengl;

import com.example.dim.opengl.shaderUtils.ShaderUtils;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

public class SimpleShaderProgram{
    public static final String MONITOR_MATRIX="u_Monitor_Matrix";
    public static final String VIEW_MATRIX="u_Matrix";
    public static final String MODEL_MATRIX="u_Model_Matrix";
    public static final String COLOR_ARRAY="a_Color";
    public static final String VERTEX_ARRAY="a_Position";
    public static final String TEXTURE_ARRAY="a_Tex_Coord";
    public static final String TEXTURE="u_texture";

    public final int monitorMatrixId;
    public final int viewMatrixId;
    public final int modelMatrixId;
    public final int colorArrayId;
    public final int vertexArrayId;
    public final int textureArrayId;
    public final int textureId;
    public final int shaderProgramId;

    public SimpleShaderProgram(String vertexShaderText,String fragmentShaderText){
        shaderProgramId= ShaderUtils.createProgram(vertexShaderText, fragmentShaderText);
        viewMatrixId = glGetUniformLocation(shaderProgramId, VIEW_MATRIX);
        monitorMatrixId =glGetUniformLocation(shaderProgramId, MONITOR_MATRIX);
        textureId = glGetUniformLocation(shaderProgramId, TEXTURE);
        modelMatrixId =glGetUniformLocation(shaderProgramId, MODEL_MATRIX);
        colorArrayId = glGetAttribLocation(shaderProgramId, COLOR_ARRAY);
        vertexArrayId = glGetAttribLocation(shaderProgramId, VERTEX_ARRAY);
        textureArrayId=glGetAttribLocation(shaderProgramId, TEXTURE_ARRAY);
        glEnableVertexAttribArray(colorArrayId);
        glEnableVertexAttribArray(textureArrayId);
        glEnableVertexAttribArray(vertexArrayId);
    }
}
