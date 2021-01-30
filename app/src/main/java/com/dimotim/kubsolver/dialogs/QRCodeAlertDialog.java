package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeAlertDialog {
    public static void showDialog(Context context, String content){
        ImageView image = new ImageView(context);
        try {
            image.setImageBitmap(new BarcodeEncoder()
                    .encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400));
        }catch (Exception e){
            Log.e(QRCodeAlertDialog.class.getCanonicalName(), "build qr code error",e);
        }

        new AlertDialog.Builder(context)
                .setMessage(content)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setView(image)
                .create()
                .show();
    }
}

