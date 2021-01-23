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
  <div class="home-main index-model">
    <div class="project-kinship-content">
      <div class="search-bar">
        <el-select filterable clearable
                  :placeholder="$t('Process Name')"
                  @change="onChange"
                  :style="inputFocusStyle"
                  v-tooltip="tooltipOption(currentItemName)"
                  size="small">
          <el-option
            v-for="work in workList"
            :key="work.id"
            :value="work.id"
            :label="work.name"
            v-tooltip="tooltipOption(work.name)"
            >
          </el-option>
        </el-select>
        <el-button type="primary"
                  icon="ri-record-circle-fill"
                  size="mini"
                  v-tooltip.small.top.start="$t('Reset')"
                  @click="reset"
                  ></el-button>
        <el-button
                  icon="el-icon-view"
                  size="mini"
                  v-tooltip.small.top="$t('Dag label display control')"
                  @click="changeLabel"
                  ></el-button>
      </div>
      <graph-grid v-if="!isLoading && !!locations.length" :isShowLabel="isShowLabel"></graph-grid>
      <template v-if="!isLoading && !locations.length">
        <m-no-data style="height: 100%;"></m-no-data>
      </template>
    </div>
    <m-spin :is-spin="isLoading" :fullscreen="false"></m-spin>
  </div>
</template>
<script>
  import { mapActions, mapState } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import graphGrid from './_source/graphGrid.vue'

  export default {
    name: 'projects-kinship-index',
    components: { graphGrid, mSpin, mNoData },
    data () {
      return {
        isLoading: true,
        isShowLabel: true,
        currentItemName: ''
      }
    },
    props: {},
    methods: {
      ...mapActions('kinship', ['getWorkFlowList', 'getWorkFlowDAG']),
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Promise Get node needs data
        Promise.all([
          // get process definition
          this.getWorkFlowList(),
          this.getWorkFlowDAG()
        ]).then((data) => {
          this.isLoading = false
        }).catch(() => {
          this.isLoading = false
        })
      },
      /**
       * reset
       */
      reset () {
        this.isLoading = true
        this.$nextTick(() => {
          this.isLoading = false
        })
      },
      async onChange (item) {
        this.isLoading = true
        this.currentItemName = item
        try {
          await this.getWorkFlowDAG(item)
        } catch (error) {
          this.$message.error(error.msg || '')
        }
        this.isLoading = false
      },
      tooltipOption (text) {
        return {
          text,
          maxWidth: '500px',
          placement: 'top',
          theme: 'dark',
          triggerEvent: 'mouseenter',
          large: false
        }
      },
      changeLabel () {
        this.isLoading = true
        this.isShowLabel = !this.isShowLabel
        this.$nextTick(() => {
          this.isLoading = false
        })
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
      }
    },
    created () {
      this.init()
    },
    computed: {
      ...mapState('kinship', ['locations', 'workList']),
      inputFocusStyle () {
        return 'width:280px'
      }
    },
    mounted () {
    }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  .project-kinship-content {
    position: relative;
    width: 100%;
    height: calc(100vh - 100px);
    background: url("./_source/img/dag_bg.png");
    .search-bar {
      position: absolute;
      right: 8px;
      top: 10px;
      z-index: 2;
      .ans-input {
        transition: width 300ms ease-in-out;
      }
    }
  }
</style>
