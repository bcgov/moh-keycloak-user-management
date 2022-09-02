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
    const minPct = 5;
    function chartSum(context) {
      let sum = 0;
      const hiddenIndices = context.chart._hiddenIndices;
      context.dataset.data.forEach((val, i) => {
        if (hiddenIndices[i] != undefined) {
          if (!hiddenIndices[i]) {
            sum += val;
          }
        } else {
          sum += val;
        }
      });
      return sum;
    }
    function chartPct(context) {
      return context.dataset.data[context.dataIndex] / chartSum(context) * 100;
    }
    return {
      chartData: {
        labels: this.pieChartData["labels"],
        datasets: [
          {
            backgroundColor: [
              "#003366",
              "#134575",
              "#265784",
              "#396a94",
              "#4c7ca3",
              "#5f8eb2",
              "#72a0c1",
              "#85b3d1",
              "#98c5e0",
              "#abd7ef",
            ],
            data: this.pieChartData["UNIQUE_USER_COUNT"],
            datalabels: {
              align: (context) => {
                return chartPct(context) < minPct ? "end" : "start";
              },
              anchor: (context) => {
                return chartPct(context) < minPct ? "end" : "center";
              },
              color: (context) => {
                return chartPct(context) < minPct ? "#003366" : "#f7f7f7";
              },
              font: {
                family: "BCSans",
                size: 11, 
              },
              offset: (context) => {
                return chartPct(context) < minPct ? -5 : -45;
              },
              rotation: (context) => {
                return chartPct(context) < minPct ? -45 : 0;
              },
            },
          },
        ],
      },
      chartOptions: {
        responsive: true,
        maintainAspectRatio: true,
        aspectRatio: 1.5,
        radius: 100,
        plugins: {
          align: "start",
          tooltip: {
            callbacks: {
              label: function (context) {
                let label = context.label.trim();
                if (label) {
                    label += ': ';
                }
                if (context.parsed !== null) {
                    label += context.parsed + ' [' + chartPct(context).toFixed(1) + '%]';
                }
                return label;
              }
            },
            displayColors: false,
            position: "nearest",
            padding: 3,
            bodyFont: {
              family: "BCSans",
              size: 14,
            },
          },
          legend: {
            maxWidth: 145,
            align: "center",
            position: "right",
            labels: {
              boxWidth: 10,
              boxHeight: 10,
              font: {
                family: "BCSans",
                size: 14,
              },
            },
          },
        },
        layout: {
          padding: {
            top: 0,
            right: 10,
            bottom: 0,
            left: 10,
          },
        },
      },
    };
  },
};
</script>

<style scoped>
</style>