package com.dimotim.kubsolver.updatecheck.model;

import lombok.Value;

@Value
public class CheckResult {
    String createdAt;
    String publishedAt;
    String tagName;
    String apkName;
    String downloadUrl;
}
