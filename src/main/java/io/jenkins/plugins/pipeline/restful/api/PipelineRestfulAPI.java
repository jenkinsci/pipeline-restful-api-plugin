package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.rest.AbstractWorkflowJobActionHandler;
import com.cloudbees.workflow.util.ModelUtil;
import com.cloudbees.workflow.util.ServeJson;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.*;
import hudson.util.HttpResponses;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.util.TimeDuration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static javax.servlet.http.HttpServletResponse.SC_CONFLICT;

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

    @RequirePOST
    public HttpResponse doAddParameter(StaplerRequest req) {
        Jenkins.get().checkPermission(Item.CONFIGURE);

        String params = req.getParameter("params");
        JSONArray array = JSONArray.fromObject(params);

        WorkflowJob job = getJob();
        ParametersDefinitionProperty paramDefPro = job.getProperty(ParametersDefinitionProperty.class);
        List<ParameterDefinition> defAll = new ArrayList<>();
        if (paramDefPro != null) {
            defAll.addAll(paramDefPro.getParameterDefinitions());
        }

        for (Object item : array) {
            JSONObject obj = (JSONObject) item;

            StringParameterDefinition str = new StringParameterDefinition(obj.getString("name"),
                    obj.getString("value"), obj.getString("desc"), true);
            defAll.add(str);

        }
        try {
            job.addProperty(new ParametersDefinitionProperty(defAll));
        } catch (IOException e) {
            e.printStackTrace();
            return HttpResponses.errorJSON(e.getMessage());
        }
        return HttpResponses.ok();
    }

    @RequirePOST
    public HttpResponse doRemoveParameter(@QueryParameter String params) {
        Jenkins.get().checkPermission(Item.CONFIGURE);

        if (StringUtils.isEmpty(params)) {
            return HttpResponses.errorJSON("params cannot be empty");
        }

        WorkflowJob job = getJob();

        ParametersDefinitionProperty paramDefPro = job.getProperty(ParametersDefinitionProperty.class);
        if (paramDefPro == null) {
            return HttpResponses.text("no ParametersDefinitionProperty found");
        }

        paramDefPro.getParameterDefinitions().removeIf(paramDef -> {
            for (String param : params.split(",")) {
                if (paramDef.getName().equals(param)) {
                    return true;
                }
            }
            return false;
        });

        if (paramDefPro.getParameterDefinitions().size() == 0) {
            try {
                job.removeProperty(paramDefPro);
            } catch (IOException e) {
                e.printStackTrace();
                return HttpResponses.errorJSON(e.getMessage());
            }
        }
        return HttpResponses.ok();
    }

    @RequirePOST
    public void doBuild(StaplerRequest req, StaplerResponse rsp, @QueryParameter TimeDuration delay) throws IOException, ServletException, InterruptedException {
        Jenkins.get().checkPermission(Item.BUILD);
        WorkflowJob job = getJob();
        if (delay == null) {
            delay = new TimeDuration(TimeUnit.MILLISECONDS.convert(job.getQuietPeriod(), TimeUnit.SECONDS));
        }

        if (!job.isBuildable()) {
            throw org.kohsuke.stapler.HttpResponses.error(SC_CONFLICT, new IOException(job.getFullName() + " is not buildable"));
        }

        IdentityBuild identityBuild = new IdentityBuild();

        String identifyCause = req.getParameter("identifyCause");
        String uuid = UUID.randomUUID().toString();
        IdentifyCause cause = new IdentifyCause(uuid, identifyCause);
        identityBuild.setCause(cause);

        // if a build is parameterized, let that take over
        ParametersDefinitionProperty pp = job.getProperty(ParametersDefinitionProperty.class);
        if (pp != null) {
            _doBuild(req, rsp, pp, cause, delay);
        } else {
            Jenkins.get().getQueue().schedule2(job, delay.getTimeInSeconds(),
                    getBuildCause(job, req), new CauseAction(cause)).getItem();
        }

        // setup a timeout of polling the build info
        long timeout = delay.as(TimeUnit.SECONDS);
        String expectedTimeout = req.getParameter("timeout");
        try {
            timeout += Integer.parseInt(expectedTimeout);
        } catch (NumberFormatException e) {
            timeout += 100;
        }

        for(int i = 0; i < timeout; i++) {
            WorkflowRun run = findBuild(job, uuid);
            if (run != null) {
                identityBuild.setBuild(run);
                break;
            }

            Thread.sleep(1000L);
        }

        rsp.serveExposedBean(req, identityBuild, req.getParameter("jsonp") == null ? Flavor.JSON : Flavor.JSONP);
    }

    @ExportedBean
    public static class IdentityBuild {
        private WorkflowRun build;
        private IdentifyCause cause;

        @Exported
        public WorkflowRun getBuild() {
            return build;
        }

        @Exported
        public IdentifyCause getCause() {
            return cause;
        }

        public void setBuild(WorkflowRun build) {
            this.build = build;
        }

        public void setCause(IdentifyCause cause) {
            this.cause = cause;
        }
    }

    public WorkflowRun findBuild(WorkflowJob job, String uuid) {
        return job.getNewBuilds().stream().filter(build -> {
            if (build == null) {
                return false;
            }
            return build.getActions(CauseAction.class).stream().anyMatch(action -> action.getCauses().stream().anyMatch(cause -> (cause instanceof IdentifyCause) && ((IdentifyCause) cause).uuid.equals(uuid)));
        }).findFirst().orElse(null);
    }

    /**
     * Interprets the form submission and schedules a build for a parameterized job.
     *
     * <p>
     * This method is supposed to be invoked from {@link ParameterizedJobMixIn#doBuild(StaplerRequest, StaplerResponse, TimeDuration)}.
     * @return
     */
    public Queue.WaitingItem _doBuild(StaplerRequest req, StaplerResponse rsp, ParametersDefinitionProperty pp, Cause cause, @QueryParameter TimeDuration delay) throws IOException, ServletException {
        if (delay==null)
            delay=new TimeDuration(TimeUnit.MILLISECONDS.convert(getJob().getQuietPeriod(), TimeUnit.SECONDS));


        List<ParameterValue> values = new ArrayList<>();

        JSONObject formData = req.getSubmittedForm();
        JSONArray a = JSONArray.fromObject(formData.get("parameter"));

        for (Object o : a) {
            JSONObject jo = (JSONObject) o;
            String name = jo.getString("name");

            ParameterDefinition d = getParameterDefinition(pp, name);
            if(d==null)
                throw new IllegalArgumentException("No such parameter definition: " + name);
            ParameterValue parameterValue = d.createValue(req, jo);
            if (parameterValue != null) {
                values.add(parameterValue);
            } else {
                throw new IllegalArgumentException("Cannot retrieve the parameter value: " + name);
            }
        }

        Queue.WaitingItem item = Jenkins.get().getQueue().schedule(
                getJob(), delay.getTimeInSeconds(), new ParametersAction(values),
                new CauseAction(new Cause.UserIdCause()), new CauseAction(cause));
        return item;
    }


    /**
     * Gets the {@link ParameterDefinition} of the given name, if any.
     */
    @CheckForNull
    public ParameterDefinition getParameterDefinition(ParametersDefinitionProperty pp, String name) {
        for (ParameterDefinition pd : pp.getParameterDefinitions())
            if (pd.getName().equals(name))
                return pd;
        return null;
    }

    /**
     * Computes the build cause, using RemoteCause or UserCause as appropriate.
     */
    @Restricted(NoExternalUse.class)
    public static CauseAction getBuildCause(ParameterizedJobMixIn.ParameterizedJob job, StaplerRequest req) {
        Cause cause;
        @SuppressWarnings("deprecation")
        hudson.model.BuildAuthorizationToken authToken = job.getAuthToken();
        if (authToken != null && authToken.getToken() != null && req.getParameter("token") != null) {
            // Optional additional cause text when starting via token
            String causeText = req.getParameter("cause");
            cause = new Cause.RemoteCause(req.getRemoteAddr(), causeText);
        } else {
            cause = new Cause.UserIdCause();
        }
        return new CauseAction(cause);
    }

    @ExportedBean
    public static class IdentifyCause extends Cause {
        private String uuid;
        private String message;

        public IdentifyCause(String uuid, String message) {
            this.uuid = uuid;
            this.message = message;
        }

        @Exported
        public String getUuid() {
            return uuid;
        }

        @Exported
        public String getMessage() {
            return message;
        }

        @Override
        public String getShortDescription() {
            String desc = String.format("Identity cause, message is %s, uuid is %s", message, uuid);
            try {
                return Jenkins.get().getMarkupFormatter().translate(desc);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return desc;
        }
    }
}
