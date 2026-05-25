<template>
  <div id="echarts-dom" class="home-chart"></div>
</template>

<script>
import * as echarts from 'echarts';
import request from "@/utils/request";

require("echarts/theme/macarons");

export default {
  name: "home_echarts",
  data() {
    return {
      option: {
        color: ['#23b7a4'],
        barWidth: 34,
        tooltip: {
          backgroundColor: 'rgba(23,50,58,.92)',
          borderWidth: 0,
          textStyle: {
            color: '#ffffff'
          }
        },
        xAxis: {
          data: [],
          axisLine: {
            lineStyle: {
              color: '#d7ecea'
            }
          },
          axisLabel: {
            color: '#78909a'
          }
        },
        yAxis: {
          type: "value",
          min: 0,
          minInterval: 1,
          interval: 1,
          splitLine: {
            lineStyle: {
              color: '#e7f4f1',
              type: 'dashed'
            }
          },
          axisLabel: {
            color: '#78909a'
          }
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: [],
            itemStyle: {
              borderRadius: [8, 8, 0, 0],
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#5aa7ff' },
                  { offset: .55, color: '#23b7a4' },
                  { offset: 1, color: '#a7df73' }
                ]
              }
            }
          },
        ],
        grid: {
          x: 40,
          y: 40,
          x2: 40,
          y2: 40,
          borderWidth: 10,
          top: '10%',
          bottom: '0%',
          containLabel: true
        }
      },
      myEcharts: '',
      chartWidth: '',
      chartHeight: '',
    };
  },
  created() {
    this.getBuildingNum()
  },
  mounted() {
    this.createEcharts()
  },
  watch: {
    //观察option的变化
    option: {
      handler(newVal, oldVal) {
        if (this.myEcharts) {
          if (newVal) {
            this.myEcharts.setOption(newVal);
          } else {
            this.myEcharts.setOption(oldVal);
          }
        } else {
          this.createEcharts();
        }
      },
      deep: true //对象内部属性的监听，关键。
    }
  },
  methods: {
    createEcharts() {
      const chartDmo = document.getElementById("echarts-dom");
      this.myEcharts = echarts.init(chartDmo, null);
      this.myEcharts.setOption(this.option, true);
    },
    getBuildingNum() {
      //xAxis.data
      request.get("/building/getBuildingName").then(res => {
        if (res.code === '0') {
          this.option.xAxis.data = res.data
          //series.data
          request.get("/room/getEachBuildingStuNum/" + res.data.length).then(result => {
            if (result.code === '0') {
              this.option.series[0].data = result.data
            }
          })
        }
      });
    },
  }
}
</script>

<style scoped>
.home-chart {
  width: 100%;
  min-width: 320px;
  height: 500px;
}
</style>
