import Reflux from 'reflux';

const LDAPConfigurationsActions = Reflux.createActions({
    testConfig: { asyncResult: true },
});

export default LDAPConfigurationsActions;