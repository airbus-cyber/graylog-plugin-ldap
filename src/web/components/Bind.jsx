import React, { Component } from 'react';
import { Button } from 'react-bootstrap';
import UserNotification from 'util/UserNotification';

class Bind extends Component {
    // Initialize the state
    constructor(props){
        super(props);

    }

    // Fetch the list on first mount
    componentDidMount() {
        this.getUser(this.props.ldap_url, this.props.user, this.props.password);
    }

    // Retrieves the list of items from the Express app
    getUser = (ldap_url, user, password) => {
        fetch('/ldap/bind/?ldap_url='+ldap_url+'&user='+user+'&password='+password)
            .then((res) => {
                if(!res.ok) {
                    UserNotification.error("Impossible to connect");
                }
                else {
                    UserNotification.success("LDAP Connection succeeded !");
                }
            })
    }

    render() {
        return(
            <div></div>
        );
    }
}

export default Bind;
