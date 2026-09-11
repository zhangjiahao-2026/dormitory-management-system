<template>
  <div ref="chart" class="home-chart" role="img" aria-label="各宿舍楼入住学生人数柱状图"></div>
</template>

<script>
import * as echarts from 'echarts';
import request from "@/utils/request";
import {markRaw} from "vue";

export default {
  name: "home_echarts",
  data() {
    return {
      option: {
        animationDuration: 650,
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          backgroundColor: 'rgba(23,50,58,.92)',
          borderWidth: 0,
          padding: [10, 12],
          formatter(params) {
            const item = params[0]
            return `${item.axisValue}<br/><strong>${item.value}</strong> 人`
          },
          textStyle: {
            color: '#ffffff'
          }
        },
        xAxis: {
          type: 'category',
          data: [],
          axisTick: { show: false },
          axisLine: {
            lineStyle: {
              color: '#d7ecea'
            }
          },
          axisLabel: {
            color: '#5f7b84',
            fontSize: 12,
            margin: 14,
            interval: 0
          }
        },
        yAxis: {
          type: "value",
          min: 0,
          minInterval: 1,
          splitNumber: 5,
          name: '人数',
          nameTextStyle: {
            color: '#78909a',
            padding: [0, 24, 8, 0]
          },
          axisLine: { show: false },
          axisTick: { show: false },
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
            barMaxWidth: 52,
            showBackground: true,
            backgroundStyle: {
              color: 'rgba(35,183,164,.055)',
              borderRadius: [8, 8, 0, 0]
            },
            label: {
              show: true,
              position: 'top',
              distance: 8,
              color: '#36515a',
              fontSize: 12,
              fontWeight: 700,
              formatter: '{c} 人'
            },
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
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 12,
                shadowColor: 'rgba(35,183,164,.24)'
              }
            }
          },
        ],
        grid: {
          left: 18,
          right: 18,
          top: 42,
          bottom: 10,
          containLabel: true
        }
      },
      myEcharts: null,
      resizeObserver: null,
    };
  },
  created() {
    this.getBuildingNum()
  },
  mounted() {
    this.createEcharts()
  },
  beforeUnmount() {
    if (this.resizeObserver) this.resizeObserver.disconnect()
    if (this.myEcharts) this.myEcharts.dispose()
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
      if (!this.$refs.chart || this.myEcharts) return
      // ECharts 实例包含复杂内部对象，不能让 Vue 深度代理。
      this.myEcharts = markRaw(echarts.init(this.$refs.chart));
      this.myEcharts.setOption(this.option, true);
      this.resizeObserver = new ResizeObserver(() => this.myEcharts && this.myEcharts.resize())
      this.resizeObserver.observe(this.$refs.chart)
    },
    getBuildingNum() {
      request.get("/building/occupancy").then(res => {
        if (res.code === '0') {
          const rows = Array.isArray(res.data) ? res.data : []
          this.option.xAxis.data = rows.map(item => item.dormBuildName || `${item.dormBuildId}号楼`)
          this.option.series[0].data = rows.map(item => Number(item.studentCount) || 0)
        }
      });
    },
  }
}
</script>

<style scoped>
.home-chart {
  width: 100%;
  min-width: 0;
  height: 100%;
  min-height: 320px;
}
</style>
