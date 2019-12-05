<template>
  <div>
    <h1>折线图示例</h1>
    <h2 id="simple">单条折线图</h2>
    <div id="simpleChart" class="chart-container" ></div>
    <h2 id="multiple">多条折线图</h2>
    <div id="multipleChart" class="chart-container" ></div>
    <h2 id="reverse">横向折线图</h2>
    <div id="reverseChart" class="chart-container tall" ></div>
  </div>
</template>

<script>
import { Line } from '../../src/index'
import { getSimpleList, getMultipleList } from '../mock/data.js'

export default {
  name: 'lineCharts',
  mounted () {
    Line.init('#simpleChart', getSimpleList())
    Line.init('#multipleChart', getMultipleList())
    Line.init('#reverseChart', getMultipleList(), { reverseAxis: true })
  }
}
</script>

<style lang="scss">
</style>

