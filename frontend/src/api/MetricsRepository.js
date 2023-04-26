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
        return this.filterLastXDay(30, yearOfActiveUserCount);
      case "6M":
        return this.filterLastXDay(182, yearOfActiveUserCount);
      case "1Y":
        return this.fillMissingDayWithZero(365, yearOfActiveUserCount);
      default:
        return this.filterLastXDay(7, yearOfActiveUserCount);
    }
  },
  filterLastXDay(numberOfDays, dates) {
    const dayAgo = new Date();
    dayAgo.setDate(dayAgo.getDate() - numberOfDays);
    const lastXDays = dates.filter(
      ({ EVENT_DATE }) => new Date(EVENT_DATE) > dayAgo
    );
    return this.fillMissingDayWithZero(numberOfDays, lastXDays);
  },
  fillMissingDayWithZero(numberOfDay, data) {
    const lastDates = [];
    for (let i = 0; i < numberOfDay; i++) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      lastDates.push(d.toISOString());
    }

    let finalDates = [];
    lastDates.reverse().forEach((date) => {
      let c = data.find((d) => d.EVENT_DATE.slice(0, 10) == date.slice(0, 10));
      if (c) {
        finalDates.push(c);
      } else {
        console.log({ EVENT_DATE: date, ACTIVE_USER_COUNT: 0 });
        finalDates.push({ EVENT_DATE: date, ACTIVE_USER_COUNT: 0 });
      }
    });

    return finalDates;
  },
};
