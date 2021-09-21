package com.dimotim.kubsolver.util;

import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

import lombok.SneakyThrows;

public class StringSerializer {
    @SneakyThrows
    public static String serializeToString(Object obj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Base64OutputStream b64os = new Base64OutputStream(baos, Base64.DEFAULT);
        ObjectOutputStream oos = new ObjectOutputStream(b64os);
        oos.writeObject(obj);
        oos.flush();
        oos.close();
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static Object deserializeFromString(String str) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        Base64InputStream b64is = new Base64InputStream(bais, Base64.DEFAULT);
        ObjectInputStream oos = new ObjectInputStream(b64is);
        return oos.readObject();
    }
}
