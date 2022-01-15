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
  <m-list-construction :title="$t('Environment manage')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group" v-if="isADMIN">
          <el-button size="mini" id="btnCreateEnvironment" @click="_create()">{{$t('Create environment')}}</el-button>
          <el-dialog
            :title="item && item.name ? $t('Edit environment') : $t('Create environment')"
            :v-if="createEnvironmentDialog"
            :visible.sync="createEnvironmentDialog"
            width="auto">
            <m-create-environment :item="item" @onUpdate="onUpdate" @close="close"></m-create-environment>
          </el-dialog>
        </template>
      </m-conditions>
    </template>

    <template slot="content">
      <template v-if="environmentList.length || total>0">
        <m-list @on-edit="_onEdit"
                :environment-list="environmentList"
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
      <template v-if="!environmentList.length && total<=0">
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
  import mCreateEnvironment from './_source/createEnvironment'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'environment-index',
    data () {
      return {
        total: null,
        isLoading: true,
        environmentList: [],
        workerGroupList: [],
        environmentWorkerGroupRelationList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
        },
        isLeft: true,
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER',
        item: {},
        createEnvironmentDialog: false
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('security', ['getEnvironmentListPaging', 'getWorkerGroupsAll']),
      /**
       * Query
       */
      _onConditions (o) {
        this.searchParams = _.assign(this.searchParams, o)
        this.searchParams.pageNo = 1
      },
      _page (val) {
        this.searchParams.pageNo = val
      },
      _pageSize (val) {
        this.searchParams.pageSize = val
      },
      _onEdit (item) {
        this.item = item
        this.createEnvironmentDialog = true
      },
      _create () {
        this.item = { workerGroupOptions: this.workerGroupList }
        this.createEnvironmentDialog = true
      },
      onUpdate () {
        this._debounceGET('false')
        this.createEnvironmentDialog = false
      },
      close () {
        this.createEnvironmentDialog = false
      },
      _getList (flag) {
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        Promise.all([this.getEnvironmentListPaging(this.searchParams), this.getWorkerGroupsAll()]).then((values) => {
          if (this.searchParams.pageNo > 1 && values[0].totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.environmentList = []
            this.environmentList = values[0].totalList
            this.total = values[0].total
            this.isLoading = false
          }
          this.workerGroupList = values[1]
          this.environmentList.forEach(item => {
            item.workerGroupOptions = this.workerGroupList
          })
        }).catch(e => {
          this.isLoading = false
        })
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
      }
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mList, mListConstruction, mConditions, mSpin, mNoData, mCreateEnvironment }
  }
</script>
