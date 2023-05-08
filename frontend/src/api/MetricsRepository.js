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
      case "1M": {
        const oneMonthAgo = new Date();
        oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
        return this.keepNumberOfDays(oneMonthAgo, yearOfActiveUserCount);
      }
      case "6M": {
        const sixMonthsAgo = new Date();
        sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
        return this.keepNumberOfDays(sixMonthsAgo, yearOfActiveUserCount);
      }
      case "1Y": {
        const oneYearAgo = new Date();
        oneYearAgo.setMonth(oneYearAgo.getMonth() - 12);
        return this.keepNumberOfDays(oneYearAgo, yearOfActiveUserCount);
      }
      default: {
        const oneWeekAgo = new Date();
        oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
        return this.keepNumberOfDays(oneWeekAgo, yearOfActiveUserCount);
      }
    }
  },
  keepNumberOfDays(pastDate, dates) {
    const filteredDays = dates.filter(
      ({ EVENT_DATE }) => new Date(EVENT_DATE) > pastDate
    );
    const numberOfDays = this.datediff(pastDate, new Date());
    return this.addEventForMissingDays(numberOfDays, filteredDays);
  },
  addEventForMissingDays(numberOfDay, data) {
    const totalDaysDates = [];
    for (let i = 0; i < numberOfDay; i++) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      totalDaysDates.push(date.toISOString());
    }

    let finalDates = [];
    totalDaysDates.reverse().forEach((date) => {
      const dateExist = data.find(
        (d) => d.EVENT_DATE?.slice(0, 10) == date.slice(0, 10)
      );
      if (dateExist) {
        finalDates.push(dateExist);
      } else {
        finalDates.push({ EVENT_DATE: date, ACTIVE_USER_COUNT: 0 });
      }
    });

    return finalDates;
  },
  datediff(first, second) {
    return Math.round((second - first) / (1000 * 60 * 60 * 24));
  },
};
