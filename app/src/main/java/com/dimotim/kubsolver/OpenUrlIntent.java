package com.dimotim.kubsolver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class OpenUrlIntent {
    public static void showDialog(Context context, String url){
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
