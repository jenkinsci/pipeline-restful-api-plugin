package io.jenkins.plugins.workflow.restful.api;

public class SCMPipeline extends Pipeline {
    private String scriptPath;

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }
}
