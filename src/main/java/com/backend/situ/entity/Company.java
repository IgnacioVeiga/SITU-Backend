package com.backend.situ.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "companies", schema = "public")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "logo_filename", nullable = false)
    private String logo_filename;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo_filename() {
        return logo_filename;
    }

    public void setLogo_filename(String logo_filename) {
        this.logo_filename = logo_filename;
    }

    public Company() {
    }

    public Company(Long id, String name, String logoPath) {
        this.id = id;
        this.name = name;
        this.logo_filename = logoPath;
    }
}
