import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';
import LDAPPluginsConfig from 'components/LDAPPluginConfiguration';
import packageJson from '../../package.json';

PluginStore.register(new PluginManifest(packageJson, {
  systemConfigurations: [
    {
      component: LDAPPluginsConfig,
      configType: 'com.airbus_cyber_security.graylog.config.LDAPPluginConfiguration',
    },
  ],
}));
