let app_config = { config: '', organizations: '[ { "id" : "", "name" :  "" } ]' };
let config_url = process.env.BASE_URL + "config.json";
let organizations_url = process.env.BASE_URL + "organizations.json";

fetch(config_url)
    .then(async response => {
        // Firefox does not always return 200 OK on the first attempt
        if (!response.ok) {
            fetch(config_url)
                .then(async response => {
                    console.log("config fetch #2");
                    return await response.json();
                });
        }
        console.log("config fetch #1");
        return await response.json();
    })
    .then((config) => {
        console.log("config assignment");
        app_config.config = config;
    })
    .catch(err => {
        console.log(err);
    });

fetch(organizations_url)
    .then(async response => {
        // Firefox does not always return 200 OK on the first attempt
        if (!response.ok) {
            fetch(organizations_url)
                .then(async response => {
                    console.log("organizations fetch #2");
                    return await response.json();
                });
        }
        console.log("organizations fetch #1");
        return await response.json();
    })
    .then((organizations) => {
        console.log("organizations assignment");
        app_config.organizations = organizations;
    })
    .catch(err => {
        console.log(err);
    });

export default app_config;
