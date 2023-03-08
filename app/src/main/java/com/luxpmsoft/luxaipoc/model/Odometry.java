package com.luxpmsoft.luxaipoc.model;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Odometry implements Serializable {
    @SerializedName("euler_angles")
    private List<Float> eulerAngles;
    @SerializedName("exposure_duration")
    private Long exposureDuration;
    private List<Float> intrinsics;
    private Long timestamp;
    private List<Float> transform;

    public List<Float> getEulerAngles() {
        return eulerAngles;
    }

    public void setEulerAngles(List<Float> eulerAngles) {
        this.eulerAngles = eulerAngles;
    }

    public Long getExposureDuration() {
        return exposureDuration;
    }

    public void setExposureDuration(Long exposureDuration) {
        this.exposureDuration = exposureDuration;
    }

    public List<Float> getIntrinsics() {
        return intrinsics;
    }

    public void setIntrinsics(List<Float> intrinsics) {
        this.intrinsics = intrinsics;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Float> getTransform() {
        return transform;
    }

    public void setTransform(List<Float> transform) {
        this.transform = transform;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this, Odometry.class) + "\n";
    }
}
