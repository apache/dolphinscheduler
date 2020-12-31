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
  <m-list-construction :title="$t('Datasource')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <el-button size="mini" @click="_create('')">{{$t('Create Datasource')}}</el-button>
          <el-dialog
            :title="item ?($t('Edit')+$t('Datasource')) : ($t('Create')+$t('Datasource'))"
            :visible.sync="dialogVisible"
            width="auto"
            :append-to-body="true">
            <m-create-data-source :item="item" @onUpdate="onUpdate" @close="close"></m-create-data-source>
          </el-dialog>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="datasourcesList.length || total>0">
        <m-list @on-update="_onUpdate" :datasources-list="datasourcesList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize"></m-list>
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
      <template v-if="!datasourcesList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="false">
      </m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mCreateDataSource from './_source/createDataSource'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'datasource-indexP',
    data () {
      return {
        // loading
        isLoading: true,
        // Total number of articles
        total: 20,
        // data sources(List)
        datasourcesList: [],
        searchParams: {
          // Number of pages per page
          pageSize: 10,
          // Number of pages
          pageNo: 1,
          // Search value
          searchVal: ''

        },
        dialogVisible: false,
        item: {}
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('datasource', ['getDatasourcesListP']),
      /**
       * create data source
       */
      _create (item) {
        this.item = item
        this.dialogVisible = true
      },
      onUpdate () {
        this._debounceGET('false')
        this.dialogVisible = false
      },
      close () {
        this.dialogVisible = false
      },
      /**
       * page
       */
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      /**
       * conditions event
       */
      _onConditions (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      /**
       * get data(List)
       */
      _getList (flag) {
        this.isLoading = !flag
        this.getDatasourcesListP(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.datasourcesList = []
            this.datasourcesList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
        }).catch(e => {
          this.isLoading = false
        })
      },
      _onUpdate () {
        this._debounceGET('false')
      }
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
    },
    components: { mList, mConditions, mSpin, mListConstruction, mNoData, mCreateDataSource }
  }
</script>
