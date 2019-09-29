<template>
  <div>
    <h1>雷达图示例</h1>
    <h2 id="simple">简单雷达图</h2>
    <div id="simpleChart" class="chart-container tall" ></div>
  </div>
</template>

<script>
import Chart from '../../src/index'
import { getMultipleList } from '../mock/data.js'

export default {
  name: 'radarCharts',
  mounted () {
    Chart.radar('#simpleChart', getMultipleList())
  }
}
</script>

<style lang="scss">
</style>

