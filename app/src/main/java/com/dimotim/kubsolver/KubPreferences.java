package com.dimotim.kubsolver;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(name = "KubPreferences", value = SharedPref.Scope.UNIQUE)
public interface KubPreferences {
    String kubState();
    boolean checkForUpdates();
    String lastUpdateCheckSuccess();
    String preLastUpdateCheckSuccess();
}
