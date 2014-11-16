/**
 * Copyright (C), NexusPad LLC
 */

package com.nexuspad.service.dataservice;

public class HostInfo {

    private String protocol;
    private String appEnv;
    private String padHost;

    public String getApiUrl() {
        return null;
    }

    public String getHostUrl() {
        return null;
    }

    private static class HostInfoHolder {
        public static HostInfo instance = new HostInfo();
    }

    private HostInfo() {
        if (ServiceConstants.NP_ENV.equals("prod")) {
            if (ServiceConstants.NP_SSL) {
                this.setProtocol("https");
            } else {
                this.setProtocol("http");
            }
        } else {
            this.setProtocol("http");
        }
    }

    public static HostInfo getHostInfo() {
        return HostInfoHolder.instance;
    }

    /*
     * Getters and Setters
     */

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAppEnv() {
        return appEnv;
    }

    public void setAppEnv(String appEnv) {
        this.appEnv = appEnv;
    }

    public String getPadHost() {
        return padHost;
    }

    public void setPadHost(String padHost) {
        this.padHost = padHost;
    }
}
