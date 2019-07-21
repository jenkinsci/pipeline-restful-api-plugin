package io.jenkins.plugins.pipeline.restful.api;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TaskListener;
import hudson.util.IOUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class HttpFlowDefinition extends FlowDefinition {
    private String url;
    private String scriptPath;
    private boolean sandbox;
    private String script;

    @DataBoundConstructor
    public HttpFlowDefinition(String url, String scriptPath) {
        this.url = url;
        this.scriptPath = scriptPath;
    }

    @Override
    public CpsFlowExecution create(FlowExecutionOwner owner, TaskListener listener, List<? extends Action> actions) throws Exception {
        try(InputStream input = new URL(url).openStream()) {
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            IOUtils.copy(input, data);
            script = data.toString();
        }
        return new CpsFlowExecution(this.script, this.sandbox, owner);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    @Extension
    public static class DescriptorImpl extends FlowDefinitionDescriptor {
        public DescriptorImpl() {
        }

        public String getDisplayName() {
            return "Pipeline script from URL";
        }
    }
}
