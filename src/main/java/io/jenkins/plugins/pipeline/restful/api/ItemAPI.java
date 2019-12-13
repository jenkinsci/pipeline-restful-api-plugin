package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.List;

@Extension
@Restricted(NoExternalUse.class)
public class ItemAPI implements RootAction {
    public static final String URL_BASE = "items";

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return "Item API";
    }

    @Override
    public String getUrlName() {
        return URL_BASE;
    }

    @ServeJson
    public List<SimpleItem> doList() {
        List<Item> items = Jenkins.get().getAllItems();
        List<SimpleItem> simpleItems = new ArrayList<>();

        items.forEach(item -> simpleItems.add(SimpleItemUtils.convert(item)));

        return simpleItems;
    }
}
