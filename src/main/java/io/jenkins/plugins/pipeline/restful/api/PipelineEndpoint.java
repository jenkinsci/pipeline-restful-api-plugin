package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.PluginWrapper;
import hudson.model.RootAction;
import java.io.IOException;
import java.util.List;
import javax.annotation.CheckForNull;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PipelineEndpoint implements RootAction {


    public static final String PIPELINE_WEBHOOK_URL = "pipeline-server-webhook";
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
        return PIPELINE_WEBHOOK_URL;
    }

    @ServeJson
    public JSONArray doGetPluginList() throws IOException {
        List<PluginWrapper> pluginList = Jenkins.get().pluginManager.getPlugins();
        JSONObject pluginJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(PluginWrapper pluginWrapper: pluginList) {
            pluginJSON.put("artifactId", pluginWrapper.getDisplayName());
            pluginJSON.put("groupId", pluginWrapper.getLongName());
            pluginJSON.put("source", new JSONObject().put("version", pluginWrapper.getVersion()));
            jsonArray.add(pluginJSON);
        }
        return jsonArray;
    }

}
