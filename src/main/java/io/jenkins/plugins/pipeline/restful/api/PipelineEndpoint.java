package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.PluginWrapper;
import hudson.model.RootAction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.jar.Attributes;
import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import hudson.model.User;
import jenkins.model.Jenkins;
import jenkins.security.ApiTokenProperty;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.*;

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
        Jenkins.get().checkPermission(Jenkins.ADMINISTER);

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

    public void doTest(StaplerResponse rsp, @QueryParameter String callback) throws IOException, ServletException {
        User user = User.current();
        if (user == null) {
            System.out.println("not login yet");
            return;
        }
        ApiTokenProperty token = user.getProperty(ApiTokenProperty.class);
        ApiTokenProperty.DescriptorImpl desc = (ApiTokenProperty.DescriptorImpl) token.getDescriptor();
        HttpResponse response = desc.doGenerateNewToken(user, "jcli-auto-");

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        StaplerResponseWrapper out = new StaplerResponseWrapper(rsp) {
            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return new ServletOutputStream(){
                    @Override
                    public void write(int b) throws IOException {
                        output.write(b);
                    }

                    @Override
                    public boolean isReady() {
                        return false;
                    }

                    @Override
                    public void setWriteListener(WriteListener writeListener) {

                    }
                };
            }
        };
        response.generateResponse(null, out, null);

        HttpURLConnection urlCon = (HttpURLConnection) new URL(callback).openConnection();
        urlCon.setRequestMethod("POST");
        urlCon.setDoOutput(true);

        String jsonToken = output.toString();

        urlCon.setFixedLengthStreamingMode(jsonToken.length());
        urlCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        try(OutputStream os = urlCon.getOutputStream()) {
            os.write(jsonToken.getBytes());
        }

        String result = "All set, you can try use jcli now. For example: jcli plugin list";
        rsp.setContentLength(result.length());
        rsp.getWriter().write(result);
    }
}
