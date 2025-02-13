package com.capstone.toolScheduler.dto;

public class ParseJobEvent {

    // e.g. "dependabot", "codescan", "secretscan"
    private String toolName;

    // Path to JSON file on disk
    private String scanFilePath;

    // Elasticsearch index name for this tenant
    private String esIndex;

    public ParseJobEvent() {
    }

    public ParseJobEvent(String toolName, String scanFilePath, String esIndex) {
        this.toolName = toolName;
        this.scanFilePath = scanFilePath;
        this.esIndex = esIndex;
    }

    // Getters & Setters

    public String getToolName() {
        return toolName;
    }
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getScanFilePath() {
        return scanFilePath;
    }
    public void setScanFilePath(String scanFilePath) {
        this.scanFilePath = scanFilePath;
    }

    public String getEsIndex() {
        return esIndex;
    }
    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }
}
