package com.dimotim.kubsolver.updatecheck.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import lombok.Value;

@Value
public class ReleaseModel {
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
