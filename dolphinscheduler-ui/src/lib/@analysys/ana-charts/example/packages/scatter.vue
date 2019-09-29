<template>
  <div>
    <h1>气泡图示例</h1>
    <h2 id="simple">气泡图</h2>
    <div id="simpleChart" class="chart-container tall" ></div>
  </div>
</template>

<script>
import Chart from '../../src/index'
import { getMultipleList } from '../mock/data.js'

export default {
  name: 'scatterCharts',
  mounted () {
    Chart.scatter('#simpleChart', getMultipleList(true))
  }
}
</script>

<style lang="scss">
</style>

