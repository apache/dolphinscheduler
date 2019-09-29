<template>
  <div class="gauge-model">
    <div class="gauge-echart">
      <div :id="id" style="height: 380px;"></div>
    </div>
    <div class="text-1" style="margin-top: -104px;">
      {{name}}
    </div>
  </div>
</template>
<script>
  import gaugeOption from './gaugeOption'

  export default {
    name: 'gauge',
    data () {
      return {}
    },
    props: {
      id: String,
      name: String,
      value: Number
    },
    methods: {},
    created () {
    },
    mounted () {
      const gauge = echarts.init(document.getElementById(this.id)) // eslint-disable-line
      gauge.setOption(gaugeOption(this.value), true)
    },
    components: {}
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .gauge-model {
    width: 100%;
    height: 360px;
    overflow: hidden;
    margin:  0 auto;
    .gauge-echart {
      width: 350px;
      margin:  auto;
      margin-bottom: -80px;
    }
    .text-1 {
      width: 100%;
      margin: 0 auto;
      font-size: 32px;
      text-align: center;
    }
    .value-p {
      height: 254px;
      line-height: 254px;
      text-align: center;
      >b {
        font-size: 100px;
        color: #333;
      }
    }
  }
</style>
