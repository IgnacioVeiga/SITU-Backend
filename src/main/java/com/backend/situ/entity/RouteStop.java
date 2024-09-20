package com.backend.situ.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "routes_stops", schema = "public")
public class RouteStop {

    @EmbeddedId
    private RouteStopId id;

    @ManyToOne
    @MapsId("routeId")
    private Route route;

    @ManyToOne
    @MapsId("stopId")
    private Stop stop;

    private Integer stopOrder;

    public RouteStopId getId() {
        return id;
    }

    public void setId(RouteStopId id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public RouteStop() {
    }

    public RouteStop(RouteStopId id, Route route, Stop stop, Integer stopOrder) {
        this.id = id;
        this.route = route;
        this.stop = stop;
        this.stopOrder = stopOrder;
    }
}
