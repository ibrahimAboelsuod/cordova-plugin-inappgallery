package com.cordova.plugin.inappgalleryplugin;

public class MediaOptions {

    public final int pageIndex;
    public final int pageSize;
    public final boolean includeVideos;

    public MediaOptions(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.includeVideos = false;
    }

    public MediaOptions(int pageIndex, int pageSize, boolean includeVideos) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.includeVideos = includeVideos;
    }
}
