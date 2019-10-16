/*
 * Copyright © - Airbus Defense and Space (SAS)- All Rights Reserved 
 * Airbus Defense and Space owns the copyright of this document. 
 */
import PropTypes from 'prop-types';
import React from 'react';
import createReactClass from 'create-react-class';
import { Button } from 'react-bootstrap';
import { BootstrapModalForm, Input } from 'components/bootstrap';
import { IfPermitted } from 'components/common';
import ObjectUtils from 'util/ObjectUtils';
import UserNotification from 'util/UserNotification';

const LDAPPluginConfiguration = createReactClass({
	displayName: 'LDAPPluginConfiguration',

	propTypes: {
		config: PropTypes.object,
		updateConfig: PropTypes.func.isRequired,
	},

	getDefaultProps() {
		return {
			config: {
				ldap_url: 'ldap://url:port',
				user: 'user',
				password: 'password',
				heap_size: 100,
				ttl: 60,
			},
		};
	},

	getInitialState() {
		return {
			config: ObjectUtils.clone(this.props.config),
		};
	},

	componentWillReceiveProps(newProps) {
		this.setState({ config: ObjectUtils.clone(newProps.config) });
	},

	_updateConfigField(field, value) {
		const update = ObjectUtils.clone(this.state.config);
		update[field] = value;
		this.setState({ config: update });
	},

	_onCheckboxClick(field, ref) {
		return () => {
			this._updateConfigField(field, this.refs[ref].getChecked());
		};
	},

	_onSelect(field) {
		return (selection) => {
			this._updateConfigField(field, selection);
		};
	},

	_onUpdate(field) {
		return e => {
			this._updateConfigField(field, e.target.value);
		};
	},

	_openModal() {
		this.refs.ldapConfigModal.open();
	},

	_closeModal() {
		this.refs.ldapConfigModal.close();
	},

	_resetConfig() {
		// Reset to initial state when the modal is closed without saving.
		this.setState(this.getInitialState());
	},

	_saveConfig() {
		this.props.updateConfig(this.state.config).then(() => {
			this._closeModal();
		});
	},

	_test_connection() {
		UserNotification.error("TODO")
	},

	render() {
		return (
			<div>
				<h3>LDAP Plugin Configuration</h3>

				<p>
					Base configuration LDAP plugin (URI, bind user and bind password).
					Note that some parameters will be stored in MongoDB without encryption.
					Graylog users with required permissions will be able to read them in the
					configuration dialog on this page.
				</p>

				<dl className="deflist">
					<dt>LDAP URI:</dt>
					<dd>
						{this.state.config.ldap_url
							? this.state.config.ldap_url
							: '[not set]'}
					</dd>

					<dt>Bind user:</dt>
					<dd>
						{this.state.config.user
							? this.state.config.user
							: '[not set]'}
					</dd>

					<dt>Bind password:</dt>
					<dd>
						{this.state.config.password
							? this.state.config.password
							: '[not set]'}
					</dd>

					<dt>Cache heap size:</dt>
					<dd>
						{this.state.config.heap_size
							? this.state.config.heap_size
							: '[not set]'}
					</dd>

					<dt>Cache TTL:</dt>
					<dd>
						{this.state.config.ttl
							? this.state.config.ttl
							: '[not set]'}
					</dd>
				</dl>

				<IfPermitted permissions="clusterconfigentry:edit">
					<Button bsStyle="info" bsSize="xs" onClick={this._openModal}>Configure</Button>
				</IfPermitted>

				<BootstrapModalForm
					ref="ldapConfigModal"
					title="Update LDAP Plugin Configuration"
					onSubmitForm={this._saveConfig}
					onModalClose={this._resetConfig}
					submitButtonText="Save">
					<fieldset>

						<Input
							id="ldap-url"
							type="text"
							label="LDAP URI"
							help={
								<span>LDAP instance URI (ldap(s)://url:port).</span>
							}
							name="ldap_url"
							value={this.state.config.ldap_url}
							onChange={this._onUpdate('ldap_url')}
						/>

						<Input
							id="user"
							type="text"
							label="LDAP bind user"
							help={
								<span>Note that this will be stored in plaintext. Please consult the documentation for
									suggested rights to assign to the underlying IAM user.</span>
							}
							name="user"
							value={this.state.config.user}
							onChange={this._onUpdate('user')}
						/>

						<Input
							id="password"
							type="text"
							label="LDAP bind password"
							help={
								<span>Note that this will be stored in plaintext. Please consult the documentation for
									suggested rights to assign to the underlying IAM user.</span>
							}
							name="password"
							value={this.state.config.password}
							onChange={this._onUpdate('password')}
						/>

						<Input
							id="heap-size"
							type="number"
							min="0"
							label="Cache Heap Size (Mib)"
							help={
								<span> Cache size in Mib. Graylog service restart is needed after change.</span>
							}
							name="heap_size"
							value={this.state.config.heap_size}
							onChange={this._onUpdate('heap_size')}
						/>

						<Input
							id="ttl"
							type="number"
							min="0"
							label="Cache TTL (seconds)"
							help={
								<span>Cache TTL in seconds. Graylog service restart is needed after change.</span>
							}
							name="ttl"
							value={this.state.config.ttl}
							onChange={this._onUpdate('ttl')}
						/>

						<Button bsStyle="info" bsSize="s" onClick={this._test_connection}>Test</Button>
					</fieldset>
				</BootstrapModalForm>
			</div>
		);
	},
});

export default LDAPPluginConfiguration;
