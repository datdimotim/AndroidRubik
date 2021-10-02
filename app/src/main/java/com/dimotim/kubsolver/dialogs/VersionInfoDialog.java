package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.dimotim.kubsolver.GitVersionInfo_;

import java.util.stream.Collectors;

public class VersionInfoDialog {
    public static void showDialog(Context context){

        String info = GitVersionInfo_.getInstance_(context).getGitInfo()
                .entrySet()
                .stream()
                .map(kv -> kv.getKey() + ": "+ kv.getValue())
                .collect(Collectors.joining("\n"));

        View view = new LinearLayout(context){{
            setPadding(50,50,50,50);
            addView(new androidx.appcompat.widget.AppCompatTextView(context){{
                setText(info);
            }});
        }};


        new AlertDialog.Builder(context)
                .setMessage("Full version info")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setView(view)
                .create()
                .show();
    }
}

