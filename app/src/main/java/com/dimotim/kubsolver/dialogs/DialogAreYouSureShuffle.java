package com.dimotim.kubsolver.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.dimotim.kubsolver.MainActivity;

public class DialogAreYouSureShuffle extends DialogFragment{
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
            .setTitle("Are you sure?").setPositiveButton("Yes", (dialog, which) -> {
                MainActivity mainActivity=(MainActivity)getActivity();
                mainActivity.renderer.shuffle();
            })
        .setNegativeButton("Cancel", (dialog, which) -> { })
        .create();
    }
}