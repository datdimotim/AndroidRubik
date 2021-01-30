package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.dimotim.kubsolver.MainActivity;


public class DialogNewKub extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int[] n = {-1};
        String[] strings = new String[127];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = (i + 2) + "x" + (i + 2) + "x" + (i + 2);
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle("New Cube")
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel())
                .setItems(strings, (dialog, which) -> {
                    n[0] = which + 2;
                    ((MainActivity) getActivity()).renderer.setNewKub(n[0]);
                })
                .create();
    }
}

