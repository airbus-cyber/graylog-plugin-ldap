/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
package com.airbus_cyber_security.graylog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Map;

public class LDAPSearch {

	private String ldapUrl;
	private String dc;
	private String user;
	private String password;
	private int timeout;

	Logger LOG = LoggerFactory.getLogger(LDAPSearch.class);

	private static final String USER_PASSWORD_ATTRIBUTE_ID = "userPassword";

	public LDAPSearch() {
		super();
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

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}


	/**
	 * Returns the result of the query on LDAP
	 * @param value the value to search
	 * @param type type of the attribute of the value
	 * @param filter attributes to return (comma-separated), if empty, all attributes are return
	 * @return a map of LDAP attribute-attribute value
	 * @throws Exception
	 */
	public Map<String, String> getSearch(String value, String type, String filter) throws Exception {
		LOG.info("LDAP : search with query {}, type : {} and returned attributes : {}", value, type, filter);
		Map<String, String> searchResult = new HashMap<>();
		try {
			DirContext context = new InitialDirContext(LDAPUtils.getEnv(this.ldapUrl, this.user, this.password, this.timeout));
			SearchControls searchControls = new SearchControls();
			if(filter != null && !filter.isEmpty()) {
				String[] filterAttributes = filter.split(",");
				searchControls.setReturningAttributes(filterAttributes);
			}
			searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String searchQuery = "(" + type + "=" + value + ")";
			LOG.info("searchQuery {}", searchQuery);
			NamingEnumeration<SearchResult> results = context.search(this.dc, searchQuery, searchControls);
			if (results.hasMore()) {
				SearchResult result = results.next();
				Attributes attributes = result.getAttributes();
				NamingEnumeration<? extends Attribute> attrEnum = attributes.getAll();
				while (attrEnum.hasMore()) {
					Attribute attribute = attrEnum.next();
					if (!USER_PASSWORD_ATTRIBUTE_ID.equals(attribute.getID())) {
						searchResult.put(attribute.getID(), attribute.get().toString());
					}

				}
			}
			return searchResult;
		} catch (NamingException e) {
			throw new NamingException(e.getMessage());
		}
	}
}
