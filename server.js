const express = require('express');
const path = require('path');
const app = express();

// Serve the static files from the React app
app.use(express.static(path.join(__dirname, './build')));

const bodyParser = require('body-parser');

// parse application/json
app.use(bodyParser.json());

app.get("/ldapAuth", (req, res) => {
    const ldap = require('ldapjs');
    const ldap_url = req.query.ldap_url;
    const user = req.query.user;
    const password = req.query.password;

    console.log("Ldap url : "+ldap_url);
    console.log("Ldap user : "+user);
    console.log("Ldap password : "+password);

    const client = ldap.createClient({
        url: ldap_url
    });

    client.bind(user, password, function (err) {
        if (err) {
            console.error("Failed to connect to LDAP with url : " + ldap_url + " and user : " + user);
            res.status(500);
            res.json("LDAP connection with url : " + ldap_url + " and user : " + user + " failed " + err);
        } else {
            console.log("Connect to LDAP with url : " + ldap_url + " and user : " + user + " - SUCCESS");
            res.status(200).send("LDAP connection with url : " + ldap_url + " and user : " + user + " success ");
        }
    })
});

const port = process.env.PORT || 5000;
app.listen(port);

console.log('App is listening on port ' + port);