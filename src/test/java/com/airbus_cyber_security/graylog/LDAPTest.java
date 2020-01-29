package com.airbus_cyber_security.graylog;

import com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.graylog.plugins.pipelineprocessor.EvaluationContext;
import org.graylog.plugins.pipelineprocessor.ast.functions.FunctionArgs;
import org.graylog.plugins.pipelineprocessor.ast.functions.ParameterDescriptor;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LDAPTest {

    private LDAP plugin;
    private LDAPSearch search;
    private ClusterConfigService clusterConfig;
    CacheManager cacheManager;
    ParameterDescriptor<String, String> queryParam;
    FunctionArgs functionArgs;
    EvaluationContext evaluationContext;
    LDAPPluginConfigurationTest config;

    @Before
    public void setUp() throws Exception {
        clusterConfig = mock(ClusterConfigService.class);
        queryParam = mock(ParameterDescriptor.class);

        search = mock(LDAPSearch.class);

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("myCache", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60))))
                .build(true);
        plugin = new LDAP(clusterConfig, search, cacheManager, queryParam);
        functionArgs = mock(FunctionArgs.class);
        evaluationContext = mock(EvaluationContext.class);
        config = new LDAPPluginConfigurationTest("ldap://ldap.fake.com", "dc=fake,dc=com",
                "cn=mock,dc=fake,dc=com", "fake");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void evaluateNominalCase() throws Exception {
        String responseQuery = "(&(objectClass=inetOrgPerson)(uid=mock))";
        Map<String, String> response = new HashMap<>();
        response.put("uid", "mock");
        response.put("givenName", "Mock");
        response.put("sn", "Fake");
        response.put("cn", "Mock-Fake");
        when(clusterConfig.get(LDAPPluginConfiguration.class)).thenReturn(config);

        when(queryParam.required(functionArgs, evaluationContext)).thenReturn(responseQuery);
        when(search.getSearch(responseQuery)).thenReturn(response);
        String actual = plugin.evaluate(functionArgs, evaluationContext);
        String expected = "uid=mock givenName=Mock sn=Fake cn=Mock-Fake";
        assertEquals(expected, actual);
    }

    @Test
    public void evaluateCacheCase() throws Exception {
        String responseQuery = "(&(objectClass=inetOrgPerson)(uid=mock))";
        String expected = "givenName=Mock uid=mock sn=Fake cn=Mock-Fake";
        String inCache = "givenName=Mock uid=mock sn=Fake cn=Mock-Fake";
        when(clusterConfig.get(LDAPPluginConfiguration.class)).thenReturn(config);

        Cache<String, String> myCache = cacheManager.getCache("myCache", String.class, String.class);
        myCache.put(responseQuery, inCache);

        when(queryParam.required(functionArgs, evaluationContext)).thenReturn(responseQuery);
        String actual = plugin.evaluate(functionArgs, evaluationContext);
        assertEquals(expected, actual);
    }

    @Test
    public void evaluateNoResultCase() throws Exception {
        String responseQuery = "(&(objectClass=inetOrgPerson)(uid=mock))";
        Map<String, String> response = new HashMap<>();
        when(clusterConfig.get(LDAPPluginConfiguration.class)).thenReturn(config);

        when(queryParam.required(functionArgs, evaluationContext)).thenReturn(responseQuery);
        when(search.getSearch(responseQuery)).thenReturn(response);
        String actual = plugin.evaluate(functionArgs, evaluationContext);
        String expected = "LDAP=noResult";
        assertEquals(expected, actual);
    }

    @Test
    public void evaluateNoConfigCase() throws Exception {
        String responseQuery = "(&(objectClass=inetOrgPerson)(uid=mock))";
        String expected = "LDAP=noConfig";
        when(queryParam.required(functionArgs, evaluationContext)).thenReturn(responseQuery);
        String actual = plugin.evaluate(functionArgs, evaluationContext);
        assertEquals(expected, actual);
    }
}
