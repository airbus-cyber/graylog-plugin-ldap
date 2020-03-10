import Reflux from 'reflux';
import URLUtils from 'util/URLUtils';
import UserNotification from 'util/UserNotification';
import fetch from 'logic/rest/FetchProvider';
import LDAPConfigurationsActions from './LDAPConfigurationsActions';

const LDAPConfigurationsStore = Reflux.createStore({
    listenables: [LDAPConfigurationsActions],
    sourceUrl: '/plugins/com.airbus_cyber_security.graylog/ldap/config',

    testConfig(config) {
        const request = {ldap_url: config.ldap_url,
            dc: config.dc,
            user: config.user,
            password: config.password,
            heap_size: config.heap_size,
            ttl: config.ttl,
            timeout: config.timeout,
        };
        const promise = fetch('POST', URLUtils.qualifyUrl(this.sourceUrl), request)
            .then(
                data => {
                    if (data.status_code == 0) {
                        UserNotification.success(data.message);
                    }
                    else {
                        UserNotification.error(data.message);
                    }
                },
                error => {
                    UserNotification.error(`LDAP authentication failed with status: ${error}`);
                });
        LDAPConfigurationsActions.testConfig.promise(promise);
    }
});

export default LDAPConfigurationsStore;