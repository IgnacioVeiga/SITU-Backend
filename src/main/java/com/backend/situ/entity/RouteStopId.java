package com.backend.situ.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class RouteStopId implements Serializable {

    private Long routeId;
    private Long stopId;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }

    public Long getStopId() {
        return stopId;
    }

    public void setStopId(Long stopId) {
        this.stopId = stopId;
    }

    public RouteStopId() {
    }

    public RouteStopId(Long routeId, Long stopId) {
        this.routeId = routeId;
        this.stopId = stopId;
    }
}
