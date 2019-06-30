package io.jenkins.plugins.sample;

import com.cloudbees.workflow.rest.AbstractWorkflowJobActionHandler;
import com.cloudbees.workflow.util.ModelUtil;
import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.util.HttpResponses;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;

@Extension
public class JobEditAPI extends AbstractWorkflowJobActionHandler {
    public static String getUrl(WorkflowJob job) {
        return ModelUtil.getFullItemUrl(job.getUrl()) + URL_BASE + "/";
    }

    @Override
    public String getUrlName() {
        return super.getUrlName() + "su";
    }

    public static String getScriptUrl(WorkflowJob job) {
        return getUrl(job) + "script";
    }

    @ServeJson
    public Pipeline doScript() {
        WorkflowJob job = getJob();
        Pipeline pipeline = new Pipeline();

        CpsFlowDefinition def = (CpsFlowDefinition) job.getDefinition();
        pipeline.setScript(def.getScript());
        pipeline.setSandbox(def.isSandbox());
        pipeline.setDisplayName(def.getDescriptor().getDisplayName());

        return pipeline;
    }

    @RequirePOST
    public HttpResponse doUpdate(@QueryParameter String script) throws IOException {
        WorkflowJob job = getJob();

        CpsFlowDefinition oldDef = (CpsFlowDefinition) job.getDefinition();
        job.setDefinition(new CpsFlowDefinition(script, oldDef == null || oldDef.isSandbox()));

        job.save();

        return HttpResponses.ok();
    }
}
