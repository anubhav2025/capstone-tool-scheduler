package com.capstone.toolScheduler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String owner;  // GitHub username
    private String personalAccessToken;
    private String repository;

    public Credential() {
    }

    public Credential(Long id, String owner, String personalAccessToken, String repository) {
        this.id = id;
        this.owner = owner;
        this.personalAccessToken = personalAccessToken;
        this.repository = repository;
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPersonalAccessToken() {
        return personalAccessToken;
    }
    public void setPersonalAccessToken(String personalAccessToken) {
        this.personalAccessToken = personalAccessToken;
    }

    public String getRepository() {
        return repository;
    }
    public void setRepository(String repository) {
        this.repository = repository;
    }
}
