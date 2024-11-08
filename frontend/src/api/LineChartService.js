import app from "../main";
import store from "../store";
import MetricsRepository from "./MetricsRepository";

export default {
  async getLineChartData() {
    let lineChartData = app.config.globalProperties.$UserCountCache;
    if (Object.keys(lineChartData).length === 0) {
      lineChartData = await MetricsRepository.get("total-active-user-count")
        .then((response) => {
          app.config.globalProperties.$UserCountCache = response.data;
          return response.data;
        })
        .catch((error) => {
          this.handleError("Line chart data fetch failed", error);
        });
    }
    return lineChartData;
  },
  createLineChartLabelsAndDataset(lineChartData) {
    const labels = [];
    const dataset = [];
    for (var key of lineChartData.entries()) {
      labels.push(new Date(key[1]["EVENT_DATE"]).toISOString().split("T")[0]);
      dataset.push(key[1]["ACTIVE_USER_COUNT"]);
    }

    return {
      labels: labels,
      dataset: dataset,
    };
  },
  formatLineChartData(format, lineChartData) {
    switch (format) {
      case "1M": {
        const oneMonthAgo = new Date();
        oneMonthAgo.setMonth(oneMonthAgo.getMonth() - 1);
        return this.keepNumberOfDays(oneMonthAgo, lineChartData);
      }
      case "6M": {
        const sixMonthsAgo = new Date();
        sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);
        return this.keepNumberOfDays(sixMonthsAgo, lineChartData);
      }
      case "1Y": {
        const oneYearAgo = new Date();
        oneYearAgo.setMonth(oneYearAgo.getMonth() - 12);
        return this.keepNumberOfDays(oneYearAgo, lineChartData);
      }
      default: {
        const oneWeekAgo = new Date();
        oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
        return this.keepNumberOfDays(oneWeekAgo, lineChartData);
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
  handleError(message, error) {
    store.commit("alert/setAlert", {
      message: message + ": " + error,
      type: "error",
    });
    window.scrollTo(0, 0);
  },
};
