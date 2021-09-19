package com.dimotim.kubsolver;

import android.content.Context;

import com.dimotim.kubsolver.shaderUtils.FileUtils;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.SneakyThrows;

@EBean(scope = EBean.Scope.Singleton)
public class GitVersionInfo {
    @RootContext
    protected Context context;

    private Map<String, String> gitInfo;

    @AfterInject
    @SneakyThrows
    public void init() {
        String gitInfo = FileUtils.readTextFromRaw(context, R.raw.git);
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(gitInfo.getBytes(StandardCharsets.UTF_8)));
        this.gitInfo = properties.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }

    public Map<String, String> getGitInfo() {
        return new HashMap<>(gitInfo);
    }

    public String getGitHash() {
        boolean isDirty = "true".equals(gitInfo.get("git.dirty"));
        String abbr = gitInfo.get("git.commit.id.abbrev");
        if (isDirty) {
            return abbr + "-dirty";
        } else {
            return abbr;
        }
    }
}
