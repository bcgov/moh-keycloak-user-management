//import fetch from 'cross-fetch';
let app_config = new Object();

//const fetch = require("node-fetch");
fetch(process.env.BASE_URL + "config.json")
    .then((response) => {
        return response.json();
    })
    .then((config) => {
        app_config.config = config;
    });

export default app_config;