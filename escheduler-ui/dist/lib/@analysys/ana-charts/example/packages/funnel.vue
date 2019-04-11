<template>
  <div>
    <h1>漏斗图示例</h1>
    <h2 id="simple">简单漏斗图</h2>
    <div id="simpleChart" class="chart-container tall" ></div>
  </div>
</template>

<script>
import Chart from '../../src/index'
import { getSimpleList } from '../mock/data.js'

export default {
  name: 'funnelCharts',
  mounted () {
    Chart.funnel('#simpleChart', getSimpleList())
  }
}
</script>

<style lang="scss">
</style>

