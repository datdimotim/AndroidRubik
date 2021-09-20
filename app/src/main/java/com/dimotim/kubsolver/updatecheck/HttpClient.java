package com.dimotim.kubsolver.updatecheck;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {
    private static final String API_BASE_URL = "https://api.github.com/repos/datdimotim/AndroidRubik/";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    private static final CheckForUpdateService apiService = retrofit.create(CheckForUpdateService.class);

    public static CheckForUpdateService getCheckForUpdateService() {
        return apiService;
    }
}
