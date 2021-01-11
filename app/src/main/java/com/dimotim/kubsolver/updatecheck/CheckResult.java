package com.dimotim.kubsolver.updatecheck;

import lombok.Value;

@Value
public class CheckResult {
    String createdAt;
    String publishedAt;
    String tagName;
    String downloadUrl;
}
