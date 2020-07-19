package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.PluginWrapper;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import jenkins.security.UpdateSiteWarningsMonitor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.CheckForNull;
import java.io.IOException;
import java.util.List;

public class WarningAction implements RootAction {
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
        return "/cli/warnings";
    }

//    @ServeJson
//    public JSONArray doGetPluginList() throws IOException {
//        JSONArray jsonArray = new JSONArray();
//
//
//        jsonArray.addAll(new UpdateSiteWarningsMonitor().getActiveCoreWarnings());
//
////        jsonArray.addAll(new UpdateSiteWarningsMonitor().getActivePluginWarningsByPlugin());
//
//        return jsonArray;
//    }

}
