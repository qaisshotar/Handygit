package com.example.handygit.Model;

import com.google.android.gms.maps.model.LatLng;

public class SelectPlaceEvent {
    private LatLng origin,destination;

    public SelectPlaceEvent(LatLng origin, LatLng destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }
}
