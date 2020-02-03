# LDAP Pipelines functions

[![Build Status](https://travis-ci.org/airbus-cyber/graylog-plugin-ldap.svg?branch=master)](https://travis-ci.org/airbus-cyber/graylog-plugin-ldap)
[![License](https://img.shields.io/badge/license-GPL--3.0-orange.svg)](https://www.gnu.org/licenses/gpl-3.0.txt)
[![GitHub Release](https://img.shields.io/badge/release-v0.1.0-blue.svg)](https://github.com/airbus-cyber/graylog-plugin-ldap/releases)

## Installation

[Download the plugin](https://github.com/airbus-cyber/graylog-plugin-ldap/releases)
and place the `.jar` file in your Graylog plugin directory. The plugin directory
is the `plugins/` folder relative from your `graylog-server` directory by default
and can be configured in your `graylog.conf` file.

Restart `graylog-server` and you are done.

## Configuration

In the configuration tab of Graylog, you will have to provide:
  * LDAP URL
  * Bind user
  * Bind password
  * Cache Heap Size
  * Cache TTL
 
 You may have to change the order into "Message Processors Configuration". Just switch between "Message Filter Chain" and 	"Pipeline Processor"

## Usage

To search with an attribute and display all fields, create a pipeline function like:
```
rule "LDAP"
when
  has_field("user_id")
then
  let result_str = LDAP(to_string($message.user_id), "uid", "");
  let result = key_value(result_str);
  set_fields(result, "LDAP-");
end
```
If you want to do the same search but displaying only the sn and givenName fields, create a pipeline function like:
```
rule "LDAP"
when
  has_field("user_id")
then
  let result_str = LDAP(to_string($message.user_id), "uid", "sn,givenName");
  let result = key_value(result_str);
  set_fields(result, "LDAP-");
end
```
## Build

This project is using Maven 3 and requires Java 8 or higher.

* Clone this repository.
* Run `mvn package` to build a JAR file.
* Optional: Run `mvn jdeb:jdeb` and `mvn rpm:rpm` to create a DEB and RPM package respectively.
* Copy generated JAR file in target directory to your Graylog plugin directory.
* Restart the Graylog.

## License

This plugin is released under version 3.0 of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.txt).