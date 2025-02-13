package com.capstone.toolScheduler.dto;

import java.util.List;

public class ScanEventDTO {

    private String tenantId;      // instead of owner/repo
    private List<ScanType> tools; // e.g. DEPENDABOT, CODESCAN, SECRETSCAN, ALL

    public ScanEventDTO() {
    }

    public ScanEventDTO(String tenantId, List<ScanType> tools) {
        this.tenantId = tenantId;
        this.tools = tools;
    }

    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<ScanType> getTools() {
        return tools;
    }
    public void setTools(List<ScanType> tools) {
        this.tools = tools;
    }
}
