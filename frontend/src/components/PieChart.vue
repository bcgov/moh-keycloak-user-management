<template>
  <div class="pieChart" style="background-color: white">
    <Pie :chart-options="chartOptions" :chart-data="chartData" />
  </div>
</template>

<script>
import { Pie } from "vue-chartjs/legacy";
import ChartDataLabels from "chartjs-plugin-datalabels";
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  ArcElement,
  LinearScale,
} from "chart.js";

ChartJS.register(
  ChartDataLabels,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  ArcElement,
  LinearScale
);

export default {
  name: "PieChart",
  components: { Pie },
  props: {
    pieChartData: {
      type: Object,
      Default: {},
    },
  },
  data() {
    return {
      chartData: {
        labels: this.pieChartData["labels"],
        datasets: [
          {
            backgroundColor: [
              "#003366",
              "#0C5297",
              "#C7DEF5",
              "#1F66AD",
              "#86bdd9",
              "#205e7e",
              "#4b82a1",
              "#3A80A6",
              "#9ad1ec",
            ],
            data: this.pieChartData["UNIQUE_USER_COUNT"],
            datalabels: {
              align: "start",
              offset: -20,
              anchor: function (context) {
                const sum = context.dataset.data.reduce(
                  (sum, current) => sum + current
                );
                const value =
                  (100 * context.dataset.data[context.dataIndex]) / sum;
                return value < 6 ? "end" : "center";
              },
              color: function (context) {
                const sum = context.dataset.data.reduce(
                  (sum, current) => sum + current
                );
                const value =
                  (100 * context.dataset.data[context.dataIndex]) / sum;
                return value < 6 ? "#003366" : "#f7f7f7";
              },
            },
          },
        ],
      },
      chartOptions: {
        responsive: true,
        maintainAspectRatio: true,
        aspectRatio: 1.5,
        radius: 60,
        plugins: {
          align: "start",
          tooltip: {
            displayColors: false,
            position: "nearest",
            padding: 3,
            bodyFont: {
              size: 10,
            },
          },
          legend: {
            align: "center",
            position: "right",
            labels: {
              boxWidth: 10,
              boxHeight: 10,
              font: {
                size: 9,
              },
            },
          },
        },
        layout: {
          padding: {
            top: 0,
            right: 5,
            bottom: 0,
            left: 5,
          },
        },
      },
    };
  },
};
</script>

<style scoped>
</style>