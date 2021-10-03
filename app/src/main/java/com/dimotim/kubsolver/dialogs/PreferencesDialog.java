package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.dimotim.kubsolver.KubPreferences_;
import com.dimotim.kubsolver.services.CheckForUpdatesManager;

public class PreferencesDialog {
    public static void showDialog(Context context){

        View view = new LinearLayout(context){{
            setPadding(50,50,50,50);
            CheckBox cb = new CheckBox(context);
            cb.setText("Check for updates");
            KubPreferences_ kubPreferences = new KubPreferences_(context);
            cb.setChecked(kubPreferences.checkForUpdates().getOr(true));

            cb.setOnCheckedChangeListener((src, isChecked) -> {
                if(isChecked){
                    CheckForUpdatesManager.setupCheckForUpdates(context);
                } else {
                    CheckForUpdatesManager.cancelCheckForUpdates(context);
                }
                kubPreferences.edit().checkForUpdates().put(isChecked).apply();
            });
            addView(cb);
        }};


        new AlertDialog.Builder(context)
                .setMessage("App preferences")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setView(view)
                .create()
                .show();
    }
}

