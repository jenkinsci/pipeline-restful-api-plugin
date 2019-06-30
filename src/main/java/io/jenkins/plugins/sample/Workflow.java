package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import java.io.IOException;

@Extension
public class Workflow implements RootAction {
    public void doCreate(@QueryParameter String name,
                         @QueryParameter String script,
                         @QueryParameter boolean sandbox) throws IOException {
        WorkflowJob wfJob = Jenkins.get().createProject(WorkflowJob.class, name);

        wfJob.setDefinition(new CpsFlowDefinition(script, sandbox));
        wfJob.save();
    }

    @RequirePOST
    public HttpResponse doUpdate(@AncestorInPath WorkflowJob job,
                                 StaplerRequest request,
                                 @QueryParameter boolean sandbox) throws IOException {
        String script = request.getParameter("script");
        String[] scripts = request.getParameterValues("script");
        if(scripts != null && scripts.length > 0) {
            script = scripts[0];
        }

        CpsFlowDefinition oldDef = (CpsFlowDefinition) job.getDefinition();
        job.setDefinition(new CpsFlowDefinition(script, sandbox));

        job.save();

        return HttpResponses.ok();
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "suren";
    }
}
