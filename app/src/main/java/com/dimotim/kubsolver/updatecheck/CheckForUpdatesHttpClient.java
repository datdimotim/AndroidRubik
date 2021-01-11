package com.dimotim.kubsolver.updatecheck;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

    public static void checkForUpdates(Context context, Consumer<CheckResult> onResult, Consumer<CheckError> onError) {
        System.out.println(getAbsoluteUrl("releases/latest"));
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
                    List<ReleaseModel.Assets> kubikApks = release.assets.stream()
                            .filter(f -> f.getName().toLowerCase().trim().startsWith("kubik"))
                            .collect(Collectors.toList());

                    if(kubikApks.size() != 1) {
                        onError.accept(new CheckError("multiple kubik apks found in release: "+kubikApks.toString(),null));
                    }else {
                        onResult.accept(new CheckResult(
                                release.getCreatedAt(),
                                release.getPublishedAt(),
                                release.getTagName(),
                                kubikApks.get(0).getBrowserDownloadUrl()
                        ));
                    }
                }catch (Exception e){
                    onError.accept(new CheckError("parse results error", e));
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                onError.accept(new CheckError("status code: "+statusCode, error));
            }
        });
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    @Value
    public static class ReleaseModel {
        @SerializedName("created_at")
        String createdAt;
        @SerializedName("published_at")
        String publishedAt;
        @SerializedName("tag_name")
        String tagName;
        List<Assets> assets;

        @Value
        public static class Assets {
            String name;
            @SerializedName("content_type")
            String contentType;
            @SerializedName("browser_download_url")
            String browserDownloadUrl;
        }
    }

    @Value
    public static class CheckResult{
        String createdAt;
        String publishedAt;
        String tagName;
        String downloadUrl;
    }

    @Value
    public static class CheckError {
        String message;
        Throwable error;
    }
}