package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.content.Context;

public class YesNoDialog {

    public static void showDialog(Context context, String message, Runnable onPositive) {
        showDialog(context, message, onPositive, ()->{});
    }

    public static void showDialog(Context context, String message, Runnable onPositive, Runnable onNegative) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton("Cancel", (dialog, which) -> {
                    onNegative.run();
                    dialog.dismiss();
                })
                .setPositiveButton("Ok", (dialog, which) -> {
                    onPositive.run();
                    dialog.dismiss();
                })
                .create()
                .show();
    }
}
