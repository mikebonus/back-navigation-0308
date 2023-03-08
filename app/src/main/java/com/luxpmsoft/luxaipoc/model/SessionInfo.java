package com.luxpmsoft.luxaipoc.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SessionInfo implements Serializable {
    private List<Stream> streams;
    private Device device;
    @SerializedName("number_of_files")
    private Integer numberOfFiles;

    public List<Stream> getStreams() {
        return streams;
    }

    public void setStreams(List<Stream> streams) {
        this.streams = streams;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public Integer getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(Integer numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    @Override
    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this, SessionInfo.class);
    }
}
