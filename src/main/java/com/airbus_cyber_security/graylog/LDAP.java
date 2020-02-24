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
	private static final String FILTER = "filter";

	private final ClusterConfigService clusterConfig;
	private LDAPSearch search;

	private CacheManager cacheManager;

	private ParameterDescriptor<String, String> queryParam = ParameterDescriptor.string(QUERY)
			.description("The query you want to submit into LDAP search.").build();
	private ParameterDescriptor<String, String> typeParam = ParameterDescriptor.string(TYPE)
			.description("The field you want to submit into LDAP API. Can be uid or cn, ...")
			.build();
	private ParameterDescriptor<String, String> filterParam = ParameterDescriptor.string(FILTER)
			.description("The LDAP attributes (comma-separated) you want to be returned").build();

	public LDAP(final ClusterConfigService clusterConfigService, LDAPSearch search, CacheManager cacheManager,
				ParameterDescriptor<String, String> queryParam, ParameterDescriptor<String, String> typeParam,
				ParameterDescriptor<String, String> filterParam){
		this.clusterConfig = clusterConfigService;
		this.search = search;
		this.cacheManager = cacheManager;
		this.queryParam = queryParam;
		this.typeParam = typeParam;
		this.filterParam = filterParam;
	}

	@Inject
	public LDAP(final ClusterConfigService clusterConfigService) {
		this.search = new LDAPSearch();
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

	private String evaluateBody(String query, String type, String filter) {
		Cache<String, String> myCache = cacheManager.getCache("myCache", String.class, String.class);

		LDAPPluginConfiguration config = clusterConfig.get(LDAPPluginConfiguration.class);
		if (config == null) {
			log.error("Config is needed, please fill it");
			return "LDAP=noConfig";
		}

		if (myCache.containsKey(query)) {
			log.info("LDAP: query {} is into cache with response {}", query, myCache.get(query));
			return myCache.get(query);
		}
		log.info("LDAP: LDAP URL is {} with base {}, user {} and password ****", config.ldapUrl(), config.dc(),
				config.user());
		search.setLdapUrl(config.ldapUrl());
		search.setDc(config.dc());
		search.setUser(config.user());
		search.setPassword(config.password());
		search.setTimeout(config.timeout());
		log.info("LDAP: Searching with query param: {}, type param: {} and filter param: {}", query, type, filter);
		try {
			Map<String, String> response = search.getSearch(query, type, filter);
			log.info("LDAP response: {}", response);
			if (response.isEmpty()) {
				log.warn("LDAP: no result");
				return "LDAP=noResult";
			}
			String responseStr = mapToString(response);
			myCache.put(query, responseStr);
			return responseStr;
		} catch (Exception e) {
			log.error("An error occurred while LDAP search", e);
			return "LDAP=failed";
		}
	}

	@Override
	public String evaluate(FunctionArgs functionArgs, EvaluationContext evaluationContext) {
		String query = queryParam.required(functionArgs, evaluationContext);
		String type = typeParam.required(functionArgs, evaluationContext);
		String filter = filterParam.required(functionArgs, evaluationContext);
		return evaluateBody(query, type, filter);
	}

	public String mapToString(Map<String, String> map) {
		StringBuilder builder = new StringBuilder();

		// Put the Map into a key=value String
		for (Entry<String, String> entry : map.entrySet()) {
			builder.append(entry.getKey() + "=" + entry.getValue().replace(" ", "-") + " ");
		}
		String result = builder.toString();
		result = result.substring(0, result.length() - 1).replace("\"", "");
		log.info("LDAP: Result {}", result);
		return result;
	}

	@Override
	public FunctionDescriptor<String> descriptor() {
		return FunctionDescriptor.<String>builder().name(NAME)
				.description("Returns Map of field return by the LDAP search").params(queryParam, typeParam, filterParam)
				.returnType(String.class).build();
	}
}