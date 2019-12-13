package io.jenkins.plugins.pipeline.restful.api;

import hudson.model.AbstractItem;
import hudson.model.Item;

public class SimpleItem {
    private String name;
    private String displayName;
    private String description;
    private String type;
    private String shortURL;
    private String url;

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

        return simpleItem;
    }
}