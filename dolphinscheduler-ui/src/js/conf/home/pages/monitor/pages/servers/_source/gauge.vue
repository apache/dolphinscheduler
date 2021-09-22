/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  import echarts from 'echarts'
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
