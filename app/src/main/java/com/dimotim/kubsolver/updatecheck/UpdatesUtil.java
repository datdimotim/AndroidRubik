package com.dimotim.kubsolver.updatecheck;

import com.dimotim.kubsolver.GitVersionInfo;
import com.dimotim.kubsolver.updatecheck.model.CheckResult;
import com.dimotim.kubsolver.updatecheck.model.ReleaseModel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.stream.Collectors;

@EBean(scope = EBean.Scope.Singleton)
public class UpdatesUtil {
    @Bean
    protected GitVersionInfo gitVersionInfo;

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
                    release.getHtmllUrl(),
                    kubikApks.get(0).getBrowserDownloadUrl()
            );
        }
    }

    public boolean isSameVersion(CheckResult checkResult){
        return checkResult.getApkName().contains(gitVersionInfo.getGitHash());
    }
}
