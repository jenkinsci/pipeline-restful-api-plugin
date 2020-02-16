package io.jenkins.plugins.pipeline.restful.api;

import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import hudson.model.Item;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;

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
    public List<SimpleItem> doList(@QueryParameter String name,
                                   @QueryParameter String type,
                                   @QueryParameter String parent,
                                   @QueryParameter int start,
                                   @QueryParameter int limit) {
        List<Item> items = Jenkins.get().getAllItems();
        List<SimpleItem> simpleItems = new ArrayList<>();

        boolean filterName = StringUtils.isNotEmpty(name);
        boolean filterType = StringUtils.isNotEmpty(type);

        start = start < 0 ? 0 : start;
        limit = limit <= 0 ? 50 : limit;

        items.stream().filter(item -> !filterName || item.getName().contains(name)).
                filter(item -> !filterType || item.getClass().getSimpleName().contains(type)).
                filter(item -> StringUtils.isBlank(parent) || item.getFullName().startsWith(parent)).
                skip(start).limit(limit).
                forEach(item -> simpleItems.add(SimpleItemUtils.convert(item)));

        return simpleItems;
    }
}
