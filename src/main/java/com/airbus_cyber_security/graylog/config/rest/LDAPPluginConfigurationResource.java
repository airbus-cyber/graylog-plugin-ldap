package com.airbus_cyber_security.graylog.config.rest;

import com.airbus_cyber_security.graylog.LDAPUtils;
import com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;
import static org.graylog2.shared.security.RestPermissions.CLUSTER_CONFIG_ENTRY_READ;

@RequiresAuthentication
@Api(value = "Ldap/Config", description = "Manage ldap settings")
@Path("/ldap/config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LDAPPluginConfigurationResource extends RestResource implements PluginRestResource {
    private final ClusterConfigService clusterConfigService;

    private static final int STATUS_OK = 0;

    private static final int STATUS_KO = -1;

    Logger log = LoggerFactory.getLogger(LDAPPluginConfigurationResource.class);

    @Inject
    public LDAPPluginConfigurationResource(final ClusterConfigService clusterConfigService) {
        this.clusterConfigService = requireNonNull(clusterConfigService);
    }

    @GET
    @Timed
    @ApiOperation(value = "Test ldap configuration")
    @RequiresPermissions({CLUSTER_CONFIG_ENTRY_READ})
    public LDAPAuthResponse testConfig() {
        LDAPPluginConfiguration config = clusterConfigService.getOrDefault(LDAPPluginConfiguration.class, LDAPPluginConfiguration.createDefault());
        try {
            log.info("Config : ldapUrl {}, user {}", config.ldapUrl(), config.user());
            DirContext context = new InitialDirContext(LDAPUtils.getEnv(config.ldapUrl(), config.user(), config.password(), config.timeout()));
            SearchControls searchControls = new SearchControls();
            searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] splitUser = config.user().split(",");
            String searchQuery = "(" + splitUser[0] + ")";
            NamingEnumeration<SearchResult> results = context.search(config.dc(), searchQuery, searchControls);
            if (results.hasMore()) {
                return new LDAPAuthResponse(STATUS_OK, "LDAP authentication with user : " + config.user() + " success!");
            }
            else {
                return new LDAPAuthResponse(STATUS_KO,"LDAP authentication with user : " + config.user() + " failed. No result found.");
            }
        } catch (NamingException e) {
            String message = "Error while connecting to LDAP: " + config.ldapUrl() + " with user: " + config.user() +
                    ". ERROR: " +e.getMessage();
            return new LDAPAuthResponse(STATUS_KO,message);
        }
    }
}
