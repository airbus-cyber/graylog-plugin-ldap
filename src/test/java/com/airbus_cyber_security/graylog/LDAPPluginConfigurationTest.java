package com.airbus_cyber_security.graylog;

import com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration;

public class LDAPPluginConfigurationTest extends LDAPPluginConfiguration {

    private String ldapUrl;

    private String dc;

    private String user;

    private String password;

    private int timeout;

    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }

    public String getDc() { return dc; }

    public void setDc(String dc) { this.dc = dc; }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public String ldapUrl() {
        return ldapUrl;
    }

    @Override
    public String dc() { return dc; }

    @Override
    public String user() {
        return user;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public int heapSize() {
        return 100;
    }

    @Override
    public int ttl() {
        return 60;
    }

    @Override
    public int timeout() {
        return 500;
    }

    @Override
    public Builder toBuilder() {
        return null;
    }

    public LDAPPluginConfigurationTest(String ldapUrl, String dc, String user, String password, int timeout){
        super();
        this.ldapUrl = ldapUrl;
        this.dc = dc;
        this.user = user;
        this.password = password;
        this.timeout = timeout;
    }
}
