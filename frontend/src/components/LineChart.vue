<template>
  <div class="lineChart" style="background-color: white">
    <LineChartGenerator :options="chartOptions" :data="chartData" />
  </div>
</template>

<script>
  import {
    CategoryScale,
    Chart as ChartJS,
    Legend,
    LinearScale,
    LineElement,
    PointElement,
    Title,
    Tooltip,
  } from "chart.js";
  import { Line as LineChartGenerator } from "vue-chartjs";

  ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
  );

  export default {
    name: "LineChart",
    components: { LineChartGenerator },
    props: {
      lineChartData: {
        type: Object,
        Default: {},
      },
    },
    data() {
      return {
        chartData: {
          labels: this.lineChartData["EVENT_DATE"],
          datasets: [
            {
              label: "Total user",
              data: this.lineChartData["ACTIVE_USER_COUNT"],
              fill: false,
              borderColor: "rgb(75, 192, 192)",
              tension: 0.1,
            },
          ],
        },
        chartOptions: {
          animation: true,
          responsive: true,
          maintainAspectRatio: false,
          interaction: {
            intersect: false,
            mode: "index",
          },
          scales: {
            x: {
              ticks: {
                maxTicksLimit: 24,
              },
            },
          },
          plugins: {
            legend: {
              display: false,
            },
            datalabels: {
              display: false,
            },
            decimation: {
              enabled: true,
              algorithm: "min-max",
            },
          },
        },
      };
    },
  };
</script>

<style scoped></style>
