<template>
  <div>
    <h1>饼状图示例</h1>
    <h2 id="simple">简单饼状图</h2>
    <div id="simpleChart" class="chart-container" ></div>
    <h2 id="ring">环形图</h2>
    <div id="ringChart" class="chart-container" ></div>
  </div>
</template>

<script>
import Chart from '../../src/index'
import { getSimpleList } from '../mock/data.js'

export default {
  name: 'pieCharts',
  mounted () {
    Chart.pie('#simpleChart', getSimpleList())
    Chart.pie('#ringChart', getSimpleList(), {
      title: '环形图',
      ring: true
    })
  }
}
</script>

<style lang="scss">
</style>

