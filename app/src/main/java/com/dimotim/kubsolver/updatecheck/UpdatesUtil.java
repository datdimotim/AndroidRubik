package com.dimotim.kubsolver.updatecheck;

import com.dimotim.kubsolver.BuildConfig;

import java.util.List;
import java.util.stream.Collectors;

public class UpdatesUtil {
    public static CheckResult parseCheckResultFromGithubResponse(ReleaseModel release){
        List<ReleaseModel.Assets> kubikApks = release.getAssets().stream()
                .filter(f -> f.getName().toLowerCase().trim().startsWith("kubik"))
                .collect(Collectors.toList());

        if(kubikApks.size() > 1) {
            throw new IllegalArgumentException("multiple kubik apks found in release: "+kubikApks.toString());
        } else if(kubikApks.size() == 0) {
            throw new IllegalArgumentException("kubik apks not found in release");
        } else {
            return new CheckResult(
                    release.getCreatedAt(),
                    release.getPublishedAt(),
                    release.getTagName(),
                    kubikApks.get(0).getName(),
                    kubikApks.get(0).getBrowserDownloadUrl()
            );
        }
    }

    public static boolean isSameVersion(CheckResult checkResult){
        return checkResult.getApkName().contains(BuildConfig.gitHash);
    }
}
