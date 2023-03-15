import { umsRequest } from "./Repository";

const resource = "/metrics";
const activeUserCountCacheKey = "ACTIVE_USER_COUNT_";

export default {
    get(queryParams) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${queryParams}`));
    },
    getTotalActiveUserCount(format) {
        const dataCached = localStorage.getItem(activeUserCountCacheKey + format);
        // If cached data exist, verify that it's less that a day old, else refresh that data
        if (dataCached) {
            const cachedData = JSON.parse(dataCached);
            const oneDayBeforeMs = new Date().getTime() - 1 * 24 * 60 * 60 * 1000;

            if (oneDayBeforeMs < new Date(cachedData?.time).getTime()) {
                return cachedData?.data;
            }
        }
        return umsRequest().then((axiosInstance) =>
            axiosInstance
                .get(`${resource}/total-active-user-count/${format}`)
                .then((response) => {
                localStorage.setItem(
                    activeUserCountCacheKey + format,
                    JSON.stringify({ data: response.data, time: new Date() })
                );
                return response.data;
                })
        );
    },
};
