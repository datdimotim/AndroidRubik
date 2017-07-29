package com.example.dim.opengl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogNewKub extends DialogFragment {
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int[] n={-1};
        String[] strings=new String[127];
        for(int i=0;i<strings.length;i++){
            strings[i]=(i+2)+"x"+(i+2)+"x"+(i+2);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("New Cube");
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                n[0] = which + 2;
                ((MainActivity)getActivity()).renderer.setNewKub(n[0]);
            }
        });
        return builder.create();
    }
}

