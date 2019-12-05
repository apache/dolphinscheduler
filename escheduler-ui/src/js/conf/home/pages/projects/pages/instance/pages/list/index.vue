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
  <m-list-construction :title="$t('Process Instance')">
    <template slot="conditions">
      <m-instance-conditions @on-query="_onQuery"></m-instance-conditions>
    </template>
    <template slot="content">
      <template v-if="processInstanceList.length">
        <m-list :process-instance-list="processInstanceList" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
        </m-list>
        <div class="page-box">
          <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!processInstanceList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import localStore from '@/module/util/localStorage'
  import { setUrlParams } from '@/module/util/routerUtil'
  import mNoData from '@/module/components/noData/noData'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mInstanceConditions from '@/conf/home/pages/projects/pages/_source/instanceConditions'

  export default {
    name: 'instance-list-index',
    data () {
      return {
        // loading
        isLoading: true,
        // 总条数
        total: null,
        // 数据
        processInstanceList: [],
        // Parameter
        searchParams: {
          // 搜索关键词
          searchVal: '',
          // 一页条数
          pageSize: 10,
          // 当前页
          pageNo: 1,
          // host
          host: '',
          // State
          stateType: '',
          // Start Time
          startDate: '',
          // End Time
          endDate: ''
        }
      }
    },
    props: {},
    methods: {
      ...mapActions('dag', ['getProcessInstance']),
      /**
       * Query
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * 分页事件
       */
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * 获取list数据
       */
      _getProcessInstanceListP (flag) {
        this.isLoading = !flag
        this.getProcessInstance(this.searchParams).then(res => {
          this.processInstanceList = []
          this.processInstanceList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      /**
       * 更新
       */
      _onUpdate () {
        this._debounceGET()
      },
      /**
       * 路由变动
       */
      _routerView () {
        return this.$route.name === 'projects-instance-details'
      },
      /**
       * 防抖请求接口
       * @desc 防止函数多次被调用
       */
      _debounceGET: _.debounce(function (flag) {
        this._getProcessInstanceListP(flag)
      }, 100, {
        'leading': false,
        'trailing': true
      })
    },
    watch: {
      // 路由变动
      '$route' (a, b) {
        if (a.name === 'instance' && b.name === 'projects-instance-details') {
          this._debounceGET()
        } else {
          // url no params get instance list
          this.searchParams.pageNo = !_.isEmpty(a.query) && a.query.pageNo || 1
        }
      },
      'searchParams': {
        deep: true,
        handler () {
          this._debounceGET()
        }
      }
    },
    created () {
      // 删除流程定义id
      localStore.removeItem('subProcessId')

      // 路由参数合并
      if (!_.isEmpty(this.$route.query)) {
        this.searchParams = _.assign(this.searchParams, this.$route.query)
      }

      // 根据路由判断请求数据
      if (!this._routerView()) {
        this._debounceGET()
      }
    },
    mounted () {
      // 轮循获取状态
      this.setIntervalP = setInterval(() => {
        this._debounceGET('false')
      }, 90000)
    },
    beforeDestroy () {
      // 销毁轮循
      clearInterval(this.setIntervalP)
    },
    components: { mList, mInstanceConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
</style>
