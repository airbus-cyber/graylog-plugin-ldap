/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
package com.airbus_cyber_security.graylog.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@JsonAutoDetect
@AutoValue
public abstract class LDAPPluginConfiguration {
	@JsonProperty("ldap_url")
	public abstract String ldapUrl();

	@JsonProperty("dc")
	public abstract String dc();

	@JsonProperty("user")
	public abstract String user();

	@JsonProperty("password")
	public abstract String password();

	@JsonProperty("heap_size")
	public abstract int heapSize();

	@JsonProperty("ttl")
	public abstract int ttl();

	@JsonProperty("timeout")
	public abstract int timeout();

	@JsonCreator
	public static LDAPPluginConfiguration create(@JsonProperty("ldap_url") String ldapUrl,
		 	@JsonProperty("dc") String dc, @JsonProperty("user") String user,
		 	@JsonProperty("password") String password, @JsonProperty("heap_size") int heapSize,
		 	@JsonProperty("ttl") int ttl, @JsonProperty("timeout") int timeout) {
		if (heapSize <= 0)
			heapSize = 100;
		if (ttl <= 0) {
			ttl = 60;
		}
		return builder().ldapUrl(ldapUrl).dc(dc).user(user).password(password).heapSize(heapSize).ttl(ttl)
				.timeout(timeout).build();
	}

	public static LDAPPluginConfiguration createDefault() {
		return builder().ldapUrl("").dc("").user("").password("").heapSize(100).ttl(60).timeout(0).build();
	}

	public static Builder builder() {
		return new AutoValue_LDAPPluginConfiguration.Builder();
	}

	public abstract Builder toBuilder();

	@AutoValue.Builder
	public static abstract class Builder {
		public abstract Builder ldapUrl(String ldapUrl);

		public abstract Builder dc(String dc);

		public abstract Builder user(String user);

		public abstract Builder password(String password);

		public abstract Builder heapSize(int heapSize);

		public abstract Builder ttl(int ttl);

		public abstract Builder timeout(int timeout);

		public abstract LDAPPluginConfiguration build();
	}

}
