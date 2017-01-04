package com.example.dim.opengl.shaderUtils;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtils {
    public static String readTextFromRaw(Context context, int resourceId) {
        StringBuilder stringBuilder = new StringBuilder();
        try(BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resourceId)))){
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\r\n");
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }
}
