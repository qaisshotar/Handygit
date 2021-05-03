package com.example.handygit.Model;

import com.firebase.geofire.GeoLocation;

public class WorkerGeoModel {
    private  String key;
    private GeoLocation geoLocation;
    private WorkerInfoMode workerInfoMode;

    public WorkerGeoModel(){

    }

    public WorkerGeoModel(String key, GeoLocation geoLocation) {
        this.key = key;
        this.geoLocation = geoLocation;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public WorkerInfoMode getWorkerInfoMode() {
        return workerInfoMode;
    }

    public void setWorkerInfoMode(WorkerInfoMode workerInfoMode) {
        this.workerInfoMode = workerInfoMode;
    }
}
