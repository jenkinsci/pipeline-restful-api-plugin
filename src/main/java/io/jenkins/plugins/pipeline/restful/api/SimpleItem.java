package io.jenkins.plugins.pipeline.restful.api;

import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import jenkins.model.ParameterizedJobMixIn;

public class SimpleItem {
    private String name;
    private String displayName;
    private String description;
    private String type;
    private String shortURL;
    private String url;

    /** comes from Job */
    private boolean buildable;
    private boolean building;
    private boolean inQueue;

    /** comes from ParameterizedJob */
    private boolean parameterized;
    private boolean disabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShortURL() {
        return shortURL;
    }

    public void setShortURL(String shortURL) {
        this.shortURL = shortURL;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isBuildable() {
        return buildable;
    }

    public void setBuildable(boolean buildable) {
        this.buildable = buildable;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public boolean isInQueue() {
        return inQueue;
    }

    public void setInQueue(boolean inQueue) {
        this.inQueue = inQueue;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean isParameterized() {
        return parameterized;
    }

    public void setParameterized(boolean parameterized) {
        this.parameterized = parameterized;
    }
}

class SimpleItemUtils {
    public static SimpleItem convert(Item item) {
        SimpleItem simpleItem = new SimpleItem();

        simpleItem.setName(item.getName());
        simpleItem.setDisplayName(item.getDisplayName());
        simpleItem.setType(item.getClass().getSimpleName());
        simpleItem.setShortURL(item.getShortUrl());
        simpleItem.setUrl(item.getUrl());

        if (item instanceof AbstractItem) {
            simpleItem.setDescription(((AbstractItem) item).getDescription());
        }

        if (item instanceof Job) {
            Job job = (Job) item;
            simpleItem.setBuildable(job.isBuildable());
            simpleItem.setBuilding(job.isBuilding());
            simpleItem.setInQueue(job.isInQueue());
        }

        if (item instanceof ParameterizedJobMixIn.ParameterizedJob) {
            ParameterizedJobMixIn.ParameterizedJob job = (ParameterizedJobMixIn.ParameterizedJob) item;
            simpleItem.setParameterized(job.isParameterized());
            simpleItem.setDisabled(job.isDisabled());
        }

        return simpleItem;
    }
}