/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
package com.airbus_cyber_security.graylog;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;
import java.util.Collection;
import java.util.Collections;

/**
 * Implement the Plugin interface here.
 */
public class LDAPPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new LDAPMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new LDAPModule());
    }
}