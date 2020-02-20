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
  <m-list-construction :title="$t('Gantt')">
    <template slot="content">
      <div class="gantt-model">
        <div class="gantt-state">
          <div class="state-tasks-color-sp">
            <a href="javascript:">
              <span>{{$t('Task Status')}}</span>
            </a>
            <a href="javascript:" v-for="(item) in tasksState" :key="item.id">
              <em class="ans-icon-rect-solid" :style="{color:item.color}"></em>
              <span>{{item.desc}}</span>
            </a>
          </div>
        </div>
        <template v-show="!isNodata">
          <div class="gantt"></div>
        </template>
        <template v-if="isNodata">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading">
        </m-spin>
      </div>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import Gantt from './_source/gantt'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import { tasksState } from '@/conf/home/pages/dag/_source/config'
  import mConditions from '@/module/components/conditions/conditions'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'instance-gantt-index',
    data () {
      return {
        // Node state
        tasksState: tasksState,
        // loading
        isLoading: true,
        // gantt data
        ganttData: {
          taskNames: []
        },
        // Data available
        isNodata: false
      }
    },
    props: {},
    methods: {
      ...mapActions('dag', ['getViewGantt']),
      /**
       * get data
       */
      _getViewGantt () {
        this.isLoading = true
        this.getViewGantt({
          processInstanceId: this.$route.params.id
        }).then(res => {
          this.ganttData = res
          if (!res.taskNames.length || !res) {
            this.isLoading = false
            this.isNodata = true
            return
          }
          // Gantt
          Gantt.init({
            el: '.gantt',
            tasks: res.tasks
          })
          setTimeout(() => {
            this.isLoading = false
          }, 200)
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {},
    created () {

    },
    mounted () {
      this._getViewGantt()
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {},
    components: { mConditions, mSecondaryMenu, mListConstruction, mSpin, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .d3-toottip {
    text-align: left;
    ul {
      li {
        overflow: hidden;
        span {
          &.sp1 {
            width: 70px;
            text-align: right;
            display: inline-block;
            padding-right: 6px;
          }
        }
      }
    }
  }
  .gantt-model {
    background: url('img/dag_bg.png');
    height: calc(100vh - 148px);
    .gantt-state {
      background: #fff;
      height: 48px;
      line-height: 48px;
      padding-left: 20px;
    }
    .gantt {
      height: calc(100vh - 220px);
      overflow-y: scroll;
    }
    rect {
      cursor: pointer;
    }
    path {
      &.link{
        fill: none;
        stroke: #666;
        stroke-width: 2px;
      }
    }
    g.tick line{
      shape-rendering: crispEdges;
    }
    .axis {
      path,line {
        fill: none;
        stroke: #000;
        shape-rendering: crispEdges;
      }
      text {
        font: 11px sans-serif;
      }
    }
    circle {
      stroke: #666;
      fill: #0097e0;
      stroke-width: 1.5px;
    }
    g.axis path {
      shape-rendering: crispEdges;
    }
  }
</style>
