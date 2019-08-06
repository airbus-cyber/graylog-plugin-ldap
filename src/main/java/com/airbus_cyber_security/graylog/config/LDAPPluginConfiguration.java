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

	@JsonProperty("user")
	public abstract String user();

	@JsonProperty("password")
	public abstract String password();

	@JsonCreator
	public static LDAPPluginConfiguration create(@JsonProperty("ldap_url") String ldapUrl,
			@JsonProperty("user") String user, @JsonProperty("password") String password) {
		return builder().ldapUrl(ldapUrl).user(user).password(password).build();
	}

	public static LDAPPluginConfiguration createDefault() {
		return builder().ldapUrl("").user("").password("").build();
	}

	public static Builder builder() {
		return new AutoValue_LDAPPluginConfiguration.Builder();
	}

	public abstract Builder toBuilder();

	@AutoValue.Builder
	public static abstract class Builder {
		public abstract Builder ldapUrl(String ldapUrl);

		public abstract Builder user(String user);

		public abstract Builder password(String password);

		public abstract LDAPPluginConfiguration build();
	}

}
