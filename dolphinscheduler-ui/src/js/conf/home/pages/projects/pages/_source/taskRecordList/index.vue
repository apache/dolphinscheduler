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
  <m-list-construction :title="config.title">
    <template slot="conditions">
      <m-conditions @on-query="_onQuery"></m-conditions>
    </template>
    <template slot="content">
      <template v-if="taskRecordList.length || total>0">
        <m-list :task-record-list="taskRecordList" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
        </m-list>
        <div class="page-box">
          <x-page :current="parseInt(searchParams.pageNo)" :total="total" show-elevator @on-change="_page"></x-page>
        </div>
      </template>
      <template v-if="!taskRecordList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import mList from './_source/list'
  import store from '@/conf/home/store'
  import mConditions from './_source/conditions'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mSecondaryMenu from '@/module/components/secondaryMenu/secondaryMenu'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'task-record-list',
    data () {
      return {
        store,
        total: null,
        taskRecordList: [],
        isLoading: true,
        searchParams: {
          taskName: '',
          state: '',
          sourceTable: '',
          destTable: '',
          taskDate: '',
          startDate: '',
          endDate: '',
          pageSize: 10,
          pageNo: 1
        }
      }
    },
    mixins: [listUrlParamHandle],
    props: {
      config: String
    },
    methods: {
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      _page (val) {
        this.searchParams.pageNo = val
      },
      /**
       * get list data
       */
      _getList (flag) {
        this.isLoading = !flag
        this.store.dispatch(`dag/${this.config.apiFn}`, this.searchParams).then(res => {
          this.taskRecordList = []
          this.taskRecordList = res.totalList
          this.total = res.total
          this.isLoading = false
        }).catch(e => {
          this.isLoading = false
        })
      },
      _onUpdate () {
        this._debounceGET()
      }
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
    },
    components: { mList, mConditions, mSpin, mListConstruction, mSecondaryMenu, mNoData }
  }
</script>
