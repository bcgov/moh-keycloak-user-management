let app_config = {};
let config_url = process.env.BASE_URL + "config.json";
let organizations_url = process.env.BASE_URL + "organizations.json";

fetch(config_url)
    .then(async response => {
        // Firefox does not always return 200 OK on the first attempt
        if (!response.ok) {
            fetch(config_url)
                .then(async response => {
                    console.log("Firefox was here");
                    return await response.json();
                });
        }
        return await response.json();
    })
    .then((config) => {
        app_config.config = config;
    });

fetch(organizations_url)
    .then(async response => {
        // Firefox does not always return 200 OK on the first attempt
        if (!response.ok) {
            fetch(organizations_url)
                .then(async response => {
                    console.log("Firefox was here");
                    return await response.json();
                });
        }
        return await response.json();
    })
    .then((organizations) => {
        app_config.organizations = organizations;
    });

export default app_config;
