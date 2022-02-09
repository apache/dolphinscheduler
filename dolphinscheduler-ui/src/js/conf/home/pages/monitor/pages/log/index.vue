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
  <m-list-construction :title="$t('Audit Log')">
    <template slot="conditions">
      <m-log-conditions @on-query="_onQuery"></m-log-conditions>
    </template>
    <template slot="content">
      <template v-if="logList.length || total>0">
        <m-list @on-edit="_onEdit"
                :log-list="logList"
                :page-no="searchParams.pageNo"
                :page-size="searchParams.pageSize">
        </m-list>
        <div class="page-box">
          <el-pagination
            background
            @current-change="_page"
            @size-change="_pageSize"
            :page-size="searchParams.pageSize"
            :current-page.sync="searchParams.pageNo"
            :page-sizes="[10, 30, 50]"
            layout="sizes, prev, pager, next, jumper"
            :total="total">
          </el-pagination>
        </div>
      </template>
      <template v-if="!logList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="isLeft"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import store from '@/conf/home/store'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mLogConditions from '@/conf/home/pages/monitor/pages/_source/conditions/audit/auditLog'

  export default {
    name: 'audit-log-index',
    data () {
      return {
        total: null,
        isLoading: true,
        logList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          // resource type
          resourceType: '',
          // operation
          operationType: '',
          // start date
          startDate: '',
          // end date
          endDate: '',
          // operator
          userName: ''
        },
        isLeft: true,
        // isEffective: 1,
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER',
        item: {}
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('projects', ['getAuditLogList']),
      /**
       * click query
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      _onUpdate () {
        this._debounceGET()
      },
      close () {
      },
      _getList (flag) {
        this.isLoading = !flag
        this.getAuditLogList(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.logList = []
            this.logList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
        }).catch(e => {
          this.isLoading = false
        })
      },
      _debounceGET: _.debounce(function (flag) {
        if (Number(sessionStorage.getItem('isLeft')) === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this._getList(flag)
      }, 100, {
        leading: false,
        trailing: true
      })
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    created () {
    },
    mounted () {
      // Cycle acquisition status
      this.setIntervalP = setInterval(() => {
        this._debounceGET('false')
      }, 90000)
    },
    beforeDestroy () {
      clearInterval(this.setIntervalP)
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mList, mListConstruction, mSpin, mNoData, mLogConditions }
  }
</script>
