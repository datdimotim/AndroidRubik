package com.dimotim.kubsolver.updatecheck;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface CheckForUpdateService {
    @GET("releases/latest")
    @Headers({
            "Accept: application/vnd.github.v3+json",
            "User-Agent: Dalvik/2.1.0 (Linux; U; Android 7.0; Redmi Note 4 MIUI/V9.5.9.0.NCFMIFA)"
    })
    Single<ReleaseModel> getLatestRelease();
}
