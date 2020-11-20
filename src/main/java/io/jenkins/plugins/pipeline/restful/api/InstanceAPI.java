package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.RootAction;
import hudson.model.User;
import hudson.remoting.VirtualChannel;
import hudson.util.HttpResponses;
import hudson.util.RemotingDiagnostics;
import jenkins.model.Jenkins;
import jenkins.model.identity.IdentityRootAction;
import jenkins.security.ApiTokenProperty;
import jenkins.slaves.JnlpSlaveAgentProtocol;
import net.sf.json.JSONObject;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
@Restricted(NoExternalUse.class)
public class InstanceAPI implements RootAction {
    private static final Logger LOGGER = Logger.getLogger(InstanceAPI.class.getName());

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Jenkins instance API";
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return "instance";
    }

    @ServeJson
    public JenkinsInstance doIndex() {
        IdentityRootAction identityAction = new IdentityRootAction();

        JenkinsInstance jenkinsInstance = new JenkinsInstance(identityAction.getFingerprint(), identityAction.getPublicKey());
        jenkinsInstance.setSystemMessage(Jenkins.get().getSystemMessage());

        return jenkinsInstance;
    }

    @ServeJson
    public Properties doSystemProperties() {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            LOGGER.severe("no permission to get system properties");
            return new Properties();
        }
        return System.getProperties();
    }

    @ServeJson
    public Map<String, String> doSystemEnv() {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            LOGGER.severe("no permission to get system environment");
            return new HashMap<>();
        }
        return System.getenv();
    }

    @RequirePOST
    public HttpResponse doUpdateMessage(StaplerRequest req) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            LOGGER.severe("no permission to set system message");
            return HttpResponses.errorJSON("no permission to set system message");
        }
        String message = req.getParameter("message");

        try {
            Jenkins.get().setSystemMessage(message);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "cannot set system message of Jenkins", e);
            return HttpResponses.errorJSON(e.getMessage());
        }

        return HttpResponses.ok();
    }

    @RequirePOST
    public HttpResponse doAgentSecret(@QueryParameter String name) {
        try {
            return HttpResponses.text(JnlpSlaveAgentProtocol.SLAVE_SECRET.mac(name));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "cannot get the slave secret", e);
            return HttpResponses.errorJSON(e.getMessage());
        }
    }

    /**
     * Generate the user token which is prepare for https://github.com/jenkins-zh/jenkins-cli
     * @param rsp
     * @param callback
     * @throws IOException
     * @throws ServletException
     */
    public void doGenerateToken(StaplerResponse rsp, @QueryParameter(required = true) String callback) throws IOException, ServletException {
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

        urlCon.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        JSONObject jsonObj = JSONObject.fromObject(output.toString());
        jsonObj.getJSONObject("data").put("userName", user.getFullName());
        urlCon.setFixedLengthStreamingMode(jsonObj.toString().length());

        try(OutputStream os = urlCon.getOutputStream()) {
            os.write(jsonObj.toString().getBytes());
        }

        String result = "All set, jcli is ready! For example: 'jcli plugin list'. You can close this page now.";
        rsp.setContentLength(result.length());
        rsp.getWriter().write(result);
    }

    @RequirePOST
    public HttpResponse doRun(@QueryParameter String script, @QueryParameter String agent) {
        if (!Jenkins.get().hasPermission(Jenkins.ADMINISTER)) {
            return HttpResponses.errorJSON("no permission to execute script on '" + agent + "'");
        }

        Computer computer = Jenkins.get().getComputer(agent);
        if (computer == null) {
            return HttpResponses.errorJSON("cannot find agent: " + agent);
        }

        VirtualChannel channel = computer.getChannel();
        if (channel == null) {
            return HttpResponses.errorJSON(agent + " is offline");
        }

        JSONObject output = new JSONObject();
        try {
            output.put("result", RemotingDiagnostics.executeGroovy(script, channel));
        } catch (IOException | InterruptedException e) {
            return HttpResponses.errorJSON(e.getMessage());
        }

        return HttpResponses.okJSON(output);
    }
}

class JenkinsInstance {
    private String fingerprint;
    private String publicKey;
    private String systemMessage;

    public JenkinsInstance(String fingerprint, String publicKey) {
        this.fingerprint = fingerprint;
        this.publicKey = publicKey;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
    }
}
