package com.airbus_cyber_security.graylog;

import com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration;
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

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

        //search = mock(LDAPSearch.class);

        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("myCache", CacheConfigurationBuilder
                        .newCacheConfigurationBuilder(String.class, String.class, ResourcePoolsBuilder.heap(100))
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(60))))
                .build(true);
        plugin = new LDAP(clusterConfig, new LDAPSearch(), cacheManager, queryParam);
        functionArgs = mock(FunctionArgs.class);
        evaluationContext = mock(EvaluationContext.class);
        config = new LDAPPluginConfigurationTest("ldap://127.0.0.1", "dc=airbus,dc=com",
                "cn=admin,dc=airbus,dc=com", "admin");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void evaluateWithLocalLDAPTest() throws Exception {
        createTestUserInLDAP();
        String responseQuery = "(&(objectClass=inetOrgPerson)(uid=jdoe1))";
        when(clusterConfig.get(LDAPPluginConfiguration.class)).thenReturn(config);
        when(queryParam.required(functionArgs, evaluationContext)).thenReturn(responseQuery);
        String actual = plugin.evaluate(functionArgs, evaluationContext);
        String expected = "uid=jdoe1 displayName=Jane-Doe givenName=Jane objectClass=top sn=Doe cn=Jane-Doe";
        assertEquals(expected, actual);
        deleteTestUserInLDAP("uid=jdoe1," + config.dc());
    }

    private void createTestUserInLDAP() throws NamingException {
        //create new user's DN
        String entryDN = "uid=jdoe1," + config.dc();
        //create new user's attributes
        Attribute cn = new BasicAttribute("cn", "Jane Doe");
        Attribute sn = new BasicAttribute("sn", "Doe");
        Attribute givenName = new BasicAttribute("givenName", "Jane");
        Attribute displayName = new BasicAttribute("displayName", "Jane Doe");
        Attribute oc = new BasicAttribute("objectClass");
        oc.add("top");
        oc.add("person");
        oc.add("organizationalPerson");
        oc.add("inetOrgPerson");

        DirContext context = new InitialDirContext(LDAPUtils.getEnv(config.ldapUrl(), config.user(), config.password()));

        BasicAttributes entry = new BasicAttributes();
        entry.put(cn);
        entry.put(sn);
        entry.put(givenName);
        entry.put(displayName);
        entry.put(oc);

        context.createSubcontext(entryDN, entry);
    }

    private void deleteTestUserInLDAP(final String dn) throws NamingException {
        DirContext context = new InitialDirContext(LDAPUtils.getEnv(config.ldapUrl(), config.user(), config.password()));
        context.unbind(dn);
    }
}
