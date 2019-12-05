<template>
  <div>
    <h1>柱状图示例</h1>
    <h2 id="simple">单条柱状图</h2>
    <div id="simpleChart" class="chart-container" ></div>
    <h2 id="multiple">多条柱状图</h2>
    <div id="multipleChart" class="chart-container" ></div>
    <h2 id="reverse">横向柱状图</h2>
    <div id="reverseChart" class="chart-container tall" ></div>
    <h2 id="mixin">折柱混合图</h2>
    <div id="mixinChart" class="chart-container tall" ></div>
    <h2 id="stack">堆叠柱状图</h2>
    <div id="stackChart" class="chart-container tall" ></div>
    <h2 id="timeline">时间轴柱状图</h2>
    <div id="timelineChart" class="chart-container tall" ></div>
  </div>
</template>

<script>
import Chart from '../../src/index'
import { getSimpleList, getMultipleList, getTimelineList } from '../mock/data.js'
import theme from '../mock/theme.json'

export default {
  name: 'barCharts',
  mounted () {
    // 注册主题
    Chart.config({
      theme: {
        name: 'test',
        data: theme,
        default: true
      }
    })
    Chart.bar('#simpleChart', getSimpleList())
    Chart.bar('#multipleChart', getMultipleList(), { theme: 'none' })
    Chart.bar('#reverseChart', getMultipleList(), { reverseAxis: true })
    Chart.bar('#mixinChart', getMultipleList(), {
      lineTypes: ['直接访问', '搜索引擎'],
      title: '',
      yAxis: [
        {
          type: 'value',
          name: '数值'
        },
        {
          type: 'value',
          name: '温度',
          min: 0,
          max: 25,
          interval: 5,
          axisLabel: {
            formatter: '{value} °C'
          }
        }
      ]
    })
    Chart.bar('#stackChart', getMultipleList(), {
      title: '',
      stack: true
    })
    Chart.bar('#timelineChart', getTimelineList(), {
      type: 'bar',
      title: '$timeline时间轴柱状图'
    })
  }
}
</script>

<style lang="scss">
</style>

