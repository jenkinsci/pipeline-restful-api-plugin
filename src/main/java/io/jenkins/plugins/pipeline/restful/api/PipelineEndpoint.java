package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.model.RootAction;
import java.util.List;
import java.util.jar.Attributes;
import javax.annotation.CheckForNull;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

@Extension
@Restricted(NoExternalUse.class)
public class PipelineEndpoint implements RootAction {
    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "PipelineEndpoint";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "jcliPluginManager";
    }

    @ServeJson
    public JSONArray doPluginList() {
        List<PluginWrapper> pluginList = Jenkins.get().pluginManager.getPlugins();
        JSONObject pluginJSON = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for(PluginWrapper pluginWrapper: pluginList) {
            Attributes attr = pluginWrapper.getManifest().getMainAttributes();

            pluginJSON.put("artifactId", attr.getValue("Short-Name"));
            pluginJSON.put("groupId", attr.getValue("Group-Id"));

            JSONObject version = new JSONObject();
            version.put("version", pluginWrapper.getVersion());
            pluginJSON.put("source", version);
            jsonArray.add(pluginJSON);
        }
        return jsonArray;
    }

}
