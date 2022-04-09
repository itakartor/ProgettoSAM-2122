package com.kartor.SAM.running;

public class MusicTrace {
    private Integer index;
    private String mediaFile;

    public MusicTrace() {
        this.index = 0;
        this.mediaFile = null;
    }

    public MusicTrace(Integer index, String mediaFile) {
        this.index = index;
        this.mediaFile = mediaFile;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(String mediaFile) {
        this.mediaFile = mediaFile;
    }
}
