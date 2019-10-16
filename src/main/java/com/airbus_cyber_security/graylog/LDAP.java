/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
package com.airbus_cyber_security.graylog;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.AbstractFunction;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionDescriptor;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration;
import com.google.inject.Inject;

public class LDAP extends AbstractFunction<String> {
	Logger log = LoggerFactory.getLogger(LDAP.class);

	public static final String NAME = "LDAP";
	private static final String QUERY = "query";
	private static final String TYPE = "type";

	private ClusterConfigService clusterConfig;
	private LDAPSearch search = new LDAPSearch();

	private CacheManager cacheManager;

	private final ParameterDescriptor<String, String> queryParam = ParameterDescriptor.string(QUERY)
			.description("The field you want to submit into LDAP search.").build();
	private final ParameterDescriptor<String, String> typeParam = ParameterDescriptor.string(TYPE)
			.description(
					"The category of the field you want to submit into LDAP search. Can be Computer, Software, ...")
			.build();

	@Inject
	public LDAP(final ClusterConfigService clusterConfigService) {
		clusterConfig = clusterConfigService;
		LDAPPluginConfiguration conf = clusterConfig.getOrDefault(LDAPPluginConfiguration.class,
				LDAPPluginConfiguration.createDefault());

		this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
				.withCache("myCache",
						CacheConfigurationBuilder
								.newCacheConfigurationBuilder(String.class, String.class,
										ResourcePoolsBuilder.heap(conf.heapSize()))
								.withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(conf.ttl()))))
				.build(true);
	}

	@Override
	public String evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
		String query = queryParam.required(functionArgs, evaluationContext);
		String type = typeParam.required(functionArgs, evaluationContext);
		String responseStr = "";
		Map<String, String> response = new HashMap<>();
		Cache<String, String> myCache = cacheManager.getCache("myCache", String.class, String.class);

		LDAPPluginConfiguration config = clusterConfig.get(LDAPPluginConfiguration.class);
		if (config == null) {
			log.error("Config is needed, please fill it");
			return "LDAP=noConfig";
		}

		if (myCache.containsKey(query)) {
			log.info("LDAP: query {} is into cache with response {}", query, myCache.get(query));
			return myCache.get(query);
		} else {
			log.info("LDAP: LDAP URL is {} with user {} and password {}", config.ldapUrl(), config.user(),
					config.password());
			search.setLdapUrl(config.ldapUrl());
			search.setUser(config.user());
			search.setPassword(config.password());
			log.info("LDAP: Searching into {} with query param: {}", type, query);
			// TODO: Add ldapsearch call
			response.put("1", "Login");
			log.info("LDAP response: {}", response);
			if (response.isEmpty()) {
				log.warn("LDAP: no result");
				return "LDAP=noResult";
			}
			responseStr = mapToString(response);
			myCache.put(query, responseStr);
			return responseStr;
		}
	}

	public String mapToString(Map<String, String> map) {
		String result;
		StringBuilder bld = new StringBuilder();

		// Put the Map into a key=value String
		for (Entry<String, String> entry : map.entrySet()) {
			bld.append(entry.getKey() + "=" + entry.getValue().replace(" ", "-") + " ");
		}
		result = bld.toString();
		result = result.substring(0, result.length() - 1).replace("\"", "");
		log.info("LDAP: Result {}", result);
		return result;
	}

	@Override
	public FunctionDescriptor<String> descriptor() {
		return FunctionDescriptor.<String>builder().name(NAME)
				.description("Returns Map of field return by the LDAP search").params(queryParam, typeParam)
				.returnType(String.class).build();
	}
}