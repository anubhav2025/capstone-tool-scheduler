package com.capstone.toolScheduler.dto;

import java.util.List;

/**
 * This DTO matches what's published by the Auth Server,
 * but it's not persisted in the Tool Scheduler's DB.
 */
public class ScanEventDTO {

    private String owner;
    private String repository;
    private String username;
    private List<ScanType> tools;

    public ScanEventDTO() {
    }

    public ScanEventDTO(String owner, String repository, String username, List<ScanType> tools) {
        this.owner = owner;
        this.repository = repository;
        this.username = username;
        this.tools = tools;
    }

    // Getters & Setters
    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepository() {
        return repository;
    }
    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public List<ScanType> getTools() {
        return tools;
    }
    public void setTools(List<ScanType> tools) {
        this.tools = tools;
    }
}
