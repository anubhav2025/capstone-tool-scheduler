package com.capstone.toolScheduler.dto;

public class ParseJobEvent {

    // e.g. "code-scan", "dependabot", "secret-scan"
    private String type;

    // path to JSON file containing alerts
    private String scanFilePath;

    public ParseJobEvent() {
    }

    public ParseJobEvent(String type, String scanFilePath) {
        this.type = type;
        this.scanFilePath = scanFilePath;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getScanFilePath() {
        return scanFilePath;
    }
    public void setScanFilePath(String scanFilePath) {
        this.scanFilePath = scanFilePath;
    }
}