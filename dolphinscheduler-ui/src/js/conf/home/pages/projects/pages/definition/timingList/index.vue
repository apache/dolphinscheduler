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
    <m-list-construction :title="$t('Timing list')">
      <template slot="operation">
      <span style=" float: right; padding-right:50px">
        <el-tooltip :content="$t('Return')" placement="top">
          <el-button type="primary" icon="el-icon-back" size="mini" @click="_close()"></el-button>
        </el-tooltip>
      </span>
      </template>
      <template slot="conditions">
        <m-timing-list-conditions class="searchNav" @on-query="_onQuery"></m-timing-list-conditions>
      </template>
      <template slot="content">
        <template v-if="scheduleListP.length || total>0">
          <m-list :schedule-list="scheduleListP" @on-update="_onUpdate" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
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
        <template v-if="!scheduleListP.length && total<=0">
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
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mTimingListConditions from '@/conf/home/pages/projects/pages/_source/conditions/timingList/processTimingList'
  import { findComponentDownward } from '@/module/util/'
  export default {
    name: 'timing-list-index',
    data () {
      return {
        total: null,
        scheduleListP: [],
        isLoading: true,
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: '',
          userId: '',
          stateType: '',
          startDate: '',
          endDate: '',
          processDefinitionCode: 0
        },
        isLeft: true
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('dag', ['getScheduleList']),
      /**
       * Query
       */
      _close () {
        this.$router.push(
          {
            name: 'projects-definition-list',
            query: { pageSize: this.$route.query.pagesize, pageNo: this.$route.query.pageno }
          }
        );
      },
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileUpdate('DEFINITION')
      },
      /**
       * Paging event
       */
      _page (val) {
        this.searchParams.pageNo = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * conditions
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
        setUrlParams(this.searchParams)
        this._debounceGET()
      },
      /**
       * get data list
       */
      _getList (flag) {
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.getScheduleList(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.data.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.scheduleListP = []
            this.scheduleListP = res.data.totalList
            this.total = res.data.total
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
      _updateList () {
        this.searchParams.pageNo = 1
        this.searchParams.searchVal = ''
        this._debounceGET()
      }
    },
    watch: {
      // Routing changes
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      },
      searchParams: {
        deep: true,
        handler () {
          this._debounceGET()
        }
      }
    },
    created () {
      // Delete process definition ID
      localStore.removeItem('subProcessId')
    },
    mounted () {

    },
    beforeDestroy () {
      // Destruction wheel
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mList, mSpin, mListConstruction, mNoData, mTimingListConditions }
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
          td:last-child {
            .el-button+.el-button {
              margin-left: 0;
            }
          }
        }
      }
    }
  }
  @media screen and (max-width: 1246px) {
    .searchNav {
      margin-bottom: 30px;
    }
  }
</style>
