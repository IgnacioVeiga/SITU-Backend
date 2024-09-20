package com.backend.situ.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "routes", schema = "public")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private Line line;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "GEOMETRY")
    private String coordinates;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Route() {
    }

    public Route(Long id, Line line, String name, String coordinates) {
        this.id = id;
        this.line = line;
        this.name = name;
        this.coordinates = coordinates;
    }
}

