let app_config = new Object();

fetch(process.env.BASE_URL + "config.json")
    .then((response) => {
        return response.json();
    })
    .then((config) => {
        app_config.config = config;
    });

export default app_config;
