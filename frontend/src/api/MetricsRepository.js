import { umsRequest } from "./Repository";
import Vue from 'vue'

const resource = "/metrics";

export default {
    get(queryParams) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${queryParams}`));
    },
    async getTotalActiveUserCount(format) {
        let yearOfActiveUserCount = Vue.prototype.UserCountCache;
        if (Object.keys(yearOfActiveUserCount).length === 0) {
            yearOfActiveUserCount = await umsRequest().then((axiosInstance) =>
                axiosInstance.get(`${resource}/total-active-user-count`).then((responce) => {
                    Vue.prototype.UserCountCache = responce.data;
                    return responce.data;
                })
            );
        }

        switch (format) {
            case '1M':
                return this.filterLastMonth(yearOfActiveUserCount)
            case '6M':
                return this.filterLastSixMonths(yearOfActiveUserCount)
            case '1Y':
                return yearOfActiveUserCount;
            default:
                return this.filterLastSevenDay(yearOfActiveUserCount)
        }
    },
    filterLastSevenDay(dates) {
        const oneWeekAgo = new Date()
        oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
        return dates.filter(({EVENT_DATE}) => (new Date(EVENT_DATE)) > oneWeekAgo);
    },
    filterLastMonth(dates) { 
        const oneMonthAgo = new Date();
        oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
        return dates.filter(({EVENT_DATE}) => (new Date(EVENT_DATE)) > oneMonthAgo);
    },
    filterLastSixMonths(dates) { 
        const sixMonthsAgo = new Date();
        sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
        return dates.filter(({EVENT_DATE}) => (new Date(EVENT_DATE)) > sixMonthsAgo);
    }
};
