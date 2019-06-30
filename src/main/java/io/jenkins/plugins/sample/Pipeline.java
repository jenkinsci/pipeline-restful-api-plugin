package io.jenkins.plugins.sample;

import java.util.Date;

public class Pipeline {
    private String script;
    private boolean sandbox;
    private String displayName;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        new Date().getTime();
        this.displayName = displayName;
    }
}
