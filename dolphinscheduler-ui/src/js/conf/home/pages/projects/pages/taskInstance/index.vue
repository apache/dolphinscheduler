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
  <div class="wrap-taskInstance">
    <m-list-construction :title="$t('Task Instance')">
      <template slot="conditions">
        <m-instance-conditions @on-query="_onQuery"></m-instance-conditions>
      </template>
      <template slot="content">
        <template v-if="taskInstanceList.length">
          <m-list :task-instance-list="taskInstanceList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
          </m-list>
          <div class="page-box">
            <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page" show-sizer :page-size-options="[10,30,50]" @on-size-change="_pageSize"></x-page>
          </div>
        </template>
        <template v-if="!taskInstanceList.length">
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
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mInstanceConditions from '@/conf/home/pages/projects/pages/_source/instanceConditions'

  export default {
    name: 'task-instance-list-index',
    data () {
      return {
        isLoading: true,
        total: null,
        taskInstanceList: [],
        searchParams: {
          // page size
          pageSize: 10,
          // page index
          pageNo: 1,
          // Query name
          searchVal: '',
          // Process instance id
          processInstanceId: '',
          // host
          host: '',
          // state
          stateType: '',
          // start date
          startDate: '',
          // end date
          endDate: '',
          // Exectuor Name
          executorName: ''
        },
        isLeft: true
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('dag', ['getTaskInstanceList']),
      /**
       * click query
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.processInstanceId = ''
        if (this.searchParams.taskName) {
          this.searchParams.taskName = ''
        }
        this.searchParams.pageNo = 1
      },
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize(val) {
        this.searchParams.pageSize = val
      },
      /**
       * get list data
       */
      _getList (flag) {
        this.isLoading = !flag
        if(this.searchParams.pageNo == undefined) {
          this.$router.push({ path: `/projects/index` })
          return false
        }
        this.getTaskInstanceList(this.searchParams).then(res => {
          this.taskInstanceList = []
          this.taskInstanceList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
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
        this._getList(flag)
      }, 100, {
        'leading': false,
        'trailing': true
      })
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        if (_.isEmpty(a.query)) {
          this.searchParams.processInstanceId = ''
        }
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    created () {
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
  .wrap-taskInstance {
    .table-box {
      overflow-y: scroll;
    }
    .table-box {
      .fixed {
        table-layout: auto;
        tr {
          th:last-child,td:last-child {
            background: inherit;
            width: 60px;
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
            padding-right: 90px;
          }
        }
      }
    }
  }
</style>
