package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.rest.AbstractWorkflowJobActionHandler;
import com.cloudbees.workflow.util.ModelUtil;
import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Item;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import java.io.IOException;

@Extension
@Restricted(NoExternalUse.class)
public class PipelineRestfulAPI extends AbstractWorkflowJobActionHandler {
    public static final String URL_BASE = "restFul";
    public static String getUrl(WorkflowJob job) {
        return ModelUtil.getFullItemUrl(job.getUrl()) + URL_BASE;
    }

    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @ServeJson
    public Pipeline doIndex() {
        Jenkins.get().checkPermission(Item.CONFIGURE);

        WorkflowJob job = getJob();
        FlowDefinition jobDef = job.getDefinition();

        ExtensionList<PipelineConvert> extensionList = Jenkins.get().getExtensionList(PipelineConvert.class);
        for(PipelineConvert convert : extensionList) {
            if(convert.accept(jobDef)) {
                return convert.convert(jobDef);
            }
        }

        return null;
    }

    @RequirePOST
    public HttpResponse doUpdate(@QueryParameter String script) throws IOException {
        Jenkins.get().checkPermission(Item.CONFIGURE);

        WorkflowJob job = getJob();
        job.setDefinition(new CpsFlowDefinition(script, true));
        job.save();

        return HttpResponses.ok();
    }
}