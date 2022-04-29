let app_config = new Object({config: '', organizations: ''});

fetch(process.env.BASE_URL + "config.json")
    .then((response) => {
        return response.json();
    })
    .then((config) => {
        app_config.config = config;
    });

fetch(process.env.BASE_URL + "organizations.json")
    .then((response) => {
        return response.json();
    })
    .then((organizations) => {
        app_config.organizations = organizations;
    });

export default app_config;
