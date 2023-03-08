package com.luxpmsoft.luxaipoc.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Stream implements Serializable {

    private Integer frequency;
    @SerializedName("number_of_frames")
    private int numberOfFrames;
    private String id;
    @SerializedName("file_extension")
    private String fileExtension;
    private List<Float> intrinsics;
    private String type;
    private List<Integer> resolution;
    private String encoding;

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public List<Float> getIntrinsics() {
        return intrinsics;
    }

    public void setIntrinsics(List<Float> intrinsics) {
        this.intrinsics = intrinsics;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getResolution() {
        return resolution;
    }

    public void setResolution(List<Integer> resolution) {
        this.resolution = resolution;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
