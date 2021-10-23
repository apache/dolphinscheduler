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
  <div ref="graph-grid" class="graph-grid"></div>
</template>
<script>
  import echarts from 'echarts'
  import { mapState } from 'vuex'
  import graphGridOption from './graphGridOption'

  export default {
    name: 'graphGrid',
    data () {
      return {}
    },
    props: {
      isShowLabel: Boolean
    },
    methods: {
      init () {
      }
    },
    created () {
    },
    mounted () {
      const graphGrid = echarts.init(this.$refs['graph-grid'])
      graphGrid.setOption(graphGridOption(this.locations, this.connects, this.sourceWorkFlowCode, this.isShowLabel), true)
      graphGrid.on('click', (params) => {
        // Jump to the definition page
        this.$router.push({ path: `/projects/${this.projectCode}/definition/list/${params.data.code}` })
      })
    },
    components: {},
    computed: {
      ...mapState('dag', ['projectCode']),
      ...mapState('kinship', ['locations', 'connects', 'sourceWorkFlowCode'])
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .graph-grid {
    width: 100%;
    height: calc(100vh - 100px);
    background: url("./img/dag_bg.png");
  }
</style>
