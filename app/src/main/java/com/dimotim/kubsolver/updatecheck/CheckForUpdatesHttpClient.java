package com.dimotim.kubsolver.updatecheck;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.message.BasicHeader;
import lombok.Value;

public class CheckForUpdatesHttpClient {
    private static final String BASE_URL = "https://api.github.com/repos/datdimotim/AndroidRubik/";

    private static final AsyncHttpClient client = new AsyncHttpClient();
    private static final Gson gson = new Gson();

    public static void checkForUpdates(Context context, Consumer<CheckResult> onResult, Consumer<RequestError> onError) {
        client.get(
                context,
                getAbsoluteUrl("releases/latest"),
                new Header[]{
                        new BasicHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json"),
                        new BasicHeader(HttpHeaders.USER_AGENT, System.getProperty("http.agent"))
                },
                new RequestParams(),
                new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String body = new String(responseBody, StandardCharsets.UTF_8);
                try {
                    ReleaseModel release = gson.fromJson(body, ReleaseModel.class);
                    List<ReleaseModel.Assets> kubikApks = release.getAssets().stream()
                            .filter(f -> f.getName().toLowerCase().trim().startsWith("kubik"))
                            .collect(Collectors.toList());

                    if(kubikApks.size() != 1) {
                        onError.accept(new RequestError("multiple kubik apks found in release: "+kubikApks.toString(),null));
                    }else {
                        onResult.accept(new CheckResult(
                                release.getCreatedAt(),
                                release.getPublishedAt(),
                                release.getTagName(),
                                kubikApks.get(0).getBrowserDownloadUrl()
                        ));
                    }
                }catch (Exception e){
                    onError.accept(new RequestError("parse results error", e));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                onError.accept(new RequestError("status code: "+statusCode, error));
            }
        });
    }

    public static void downloadFile(Context context, String url, File file,  Runnable onResult, Consumer<RequestError> onError) {
        client.get(url, new FileAsyncHttpResponseHandler(file, false) {
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                file.delete();
                onError.accept(new RequestError("download file error, status code="+statusCode,throwable));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, File file) {
                onResult.run();
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    @Value
    public static class RequestError {
        String message;
        Throwable error;
    }
}