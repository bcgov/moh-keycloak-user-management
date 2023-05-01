import Vue from "vue";
import { umsRequest } from "./Repository";

const resource = "/metrics";

export default {
  get(queryParams) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${queryParams}`)
    );
  },
  async getTotalActiveUserCount(format) {
    let yearOfActiveUserCount = Vue.prototype.$UserCountCache;
    if (Object.keys(yearOfActiveUserCount).length === 0) {
      yearOfActiveUserCount = await umsRequest().then((axiosInstance) =>
        axiosInstance
          .get(`${resource}/total-active-user-count`)
          .then((responce) => {
            Vue.prototype.$UserCountCache = responce.data;
            return responce.data;
          })
      );
    }

    switch (format) {
      case "1M":
        return this.keepNumberOfDays(30, yearOfActiveUserCount);
      case "6M":
        return this.keepNumberOfDays(182, yearOfActiveUserCount);
      case "1Y":
        return this.addDateForMissingDays(365, yearOfActiveUserCount);
      default:
        return this.keepNumberOfDays(7, yearOfActiveUserCount);
    }
  },
  keepNumberOfDays(numberOfDays, dates) {
    const date = new Date();
    date.setDate(date.getDate() - numberOfDays);
    const filteredDays = dates.filter(
      ({ EVENT_DATE }) => new Date(EVENT_DATE) > date
    );
    return this.addDateForMissingDays(numberOfDays, filteredDays);
  },
  addDateForMissingDays(numberOfDay, data) {
    const lastDates = [];
    for (let i = 0; i < numberOfDay; i++) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      lastDates.push(date.toISOString());
    }

    let finalDates = [];
    lastDates.reverse().forEach((date) => {
      let c = data.find((d) => d.EVENT_DATE.slice(0, 10) == date.slice(0, 10));
      if (c) {
        finalDates.push(c);
      } else {
        finalDates.push({ EVENT_DATE: date, ACTIVE_USER_COUNT: 0 });
      }
    });

    return finalDates;
  },
};
