let app_config = { config: '', organizations: '' };

fetch(process.env.BASE_URL + "config.json")
    .then(async response => {
        return await response.json();
    })
    .then((config) => {
        app_config.config = config;
    });

fetch(process.env.BASE_URL + "organizations.json")
    .then(async response => {
        return await response.json();
    })
    .then((organizations) => {
        app_config.organizations = organizations;
    });

export default app_config;
