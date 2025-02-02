package com.capstone.toolScheduler.services.tools;

public interface ToolScanService {

    /**
     * @return Unique name for this tool (e.g. "dependabot", "codescan", "secretscan").
     */
    String getToolName();

    /**
     * Fetch alerts or any data from GitHub for this tool.
     * 
     * @param owner      The repository owner or GitHub username.
     * @param repository The repository name.
     * @param token      The PAT from the credentials DB.
     * @return           A JSON string or some result for this tool’s data.
     */
    String fetchAlerts(String owner, String repository, String token);
}
