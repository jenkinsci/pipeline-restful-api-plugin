package io.jenkins.plugins.pipeline.restful.api;

public class ScriptPipeline extends Pipeline {
    private String script;
    private boolean sandbox;

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
}
