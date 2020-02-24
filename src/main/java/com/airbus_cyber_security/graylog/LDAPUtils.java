package com.airbus_cyber_security.graylog;

import javax.naming.Context;
import java.util.Hashtable;

public class LDAPUtils {

    /**
     * Gets the environment for LDAP to use
     * @param ldapUrl
     * @param user
     * @param password
     * @param timeout
     * @return
     */
    public static Hashtable<String, Object> getEnv(final String ldapUrl, final String user, final String password,
                       final int timeout) {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        if (user != null) {
            env.put(Context.SECURITY_PRINCIPAL, user);
        }
        if (password != null) {
            env.put(Context.SECURITY_CREDENTIALS, password);
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(timeout));
        return env;
    }
}
