/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
package com.airbus_cyber_security.graylog;

public class LDAPSearch {

	private String ldapUrl;
	private String dc;
	private String user;
	private String password;
	private String filter;

	public LDAPSearch() {
		super();
	}
	
	public LDAPSearch(String ldapUrl, String dc, String user, String password, String filter) {
		super();
		this.ldapUrl = ldapUrl;
		this.dc = dc;
		this.user = user;
		this.password = password;
		this.filter = filter;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getLdapUrl() {
		return ldapUrl;
	}

	public void setLdapUrl(String ldapUrl) {
		this.ldapUrl = ldapUrl;
	}

	public String getDc() {
		return dc;
	}

	public void setDc(String dc) {
		this.dc = dc;
	}

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

}