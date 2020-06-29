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
  <div class="wrap-table">
    <m-list-construction :title="$t('Process Instance')">
      <template slot="conditions">
        <m-instance-conditions @on-query="_onQuery"></m-instance-conditions>
      </template>
      <template slot="content">
        <template v-if="processInstanceList.length || total>0">
          <m-list :process-instance-list="processInstanceList" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
          </m-list>
          <div class="page-box">
            <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page" show-sizer :page-size-options="[10,30,50]" @on-size-change="_pageSize"></x-page>
          </div>
        </template>
        <template v-if="!processInstanceList.length && total<=0">
          <m-no-data></m-no-data>
        </template>
        <m-spin :is-spin="isLoading" :is-left="isLeft"></m-spin>
      </template>
    </m-list-construction>
  </div>
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
        // total
        total: null,
        // data
        processInstanceList: [],
        // Parameter
        searchParams: {
          // Search keywords
          searchVal: '',
          // Number of pages
          pageSize: 10,
          // Current page
          pageNo: 1,
          // host
          host: '',
          // State
          stateType: '',
          // Start Time
          startDate: '',
          // End Time
          endDate: '',
          // Exectuor Name
          executorName: ''
        },
        isLeft: true
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
       * Paging event
       */
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      _pageSize(val) {
        this.searchParams.pageSize = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * get list data
       */
      _getProcessInstanceListP (flag) {
        this.isLoading = !flag
        this.getProcessInstance(this.searchParams).then(res => {
          if(this.searchParams.pageNo>1 && res.totalList.length == 0) {
            this.searchParams.pageNo = this.searchParams.pageNo -1
          } else {
            this.processInstanceList = []
            this.processInstanceList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
        }).catch(e => {
          this.isLoading = false
        })
      },
      /**
       * update
       */
      _onUpdate () {
        this._debounceGET()
      },
      /**
       * Routing changes
       */
      _routerView () {
        return this.$route.name === 'projects-instance-details'
      },
      /**
       * Anti shake request interface
       * @desc Prevent functions from being called multiple times
       */
      _debounceGET: _.debounce(function (flag) {
        if(sessionStorage.getItem('isLeft')==0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this._getProcessInstanceListP(flag)
      }, 100, {
        'leading': false,
        'trailing': true
      })
    },
    watch: {
      // Routing changes
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
      // Delete process definition ID
      localStore.removeItem('subProcessId')

      // Route parameter merge
      if (!_.isEmpty(this.$route.query)) {
        this.searchParams = _.assign(this.searchParams, this.$route.query)
      }

      // Judge the request data according to the route
      if (!this._routerView()) {
        this._debounceGET()
      }
    },
    mounted () {
      this.$modal.destroy()
      // Cycle acquisition status
      this.setIntervalP = setInterval(() => {
        this._debounceGET('false')
      }, 90000)
    },
    beforeDestroy () {
      // Destruction wheel
      clearInterval(this.setIntervalP)
      sessionStorage.setItem('isLeft',1)
    },
    components: { mList, mInstanceConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .wrap-table {
    .table-box {
      overflow-y: scroll;
    }
    .table-box {
      .fixed {
        table-layout: auto;
        tr {
          th:last-child,td:last-child {
            background: inherit;
            width: 230px;
            height: 40px;
            line-height: 40px;
            border-left:1px solid #ecf3ff;
            position: absolute;
            right: 0;
            z-index: 2;
          }
          td:last-child {
            border-bottom:1px solid #ecf3ff;
          }
          th:nth-last-child(2) {
            padding-right: 260px;
          }
        }
      }
    }
  }
</style>
