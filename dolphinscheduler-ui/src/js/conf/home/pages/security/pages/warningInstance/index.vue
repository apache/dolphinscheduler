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
  <m-list-construction :title="$t('Warning instance manage')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group" v-if="isADMIN">
          <el-button size="mini" @click="_create('')">{{$t('Create Alarm Instance')}}</el-button>
          <el-dialog
            :title="item ? $t('Edit Alarm Instance') : $t('Create Alarm Instance')"
            v-if="createWarningDialog"
            :visible.sync="createWarningDialog"
            width="auto">
            <m-create-warning-instance :item="item" :pulginInstance="pulginInstance" @onUpdate="onUpdate" @close="close"></m-create-warning-instance>
          </el-dialog>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="alertgroupList!==null || total>0 ">
        <m-list @on-edit="_onEdit"
                @on-update="_onUpdate"
                :alertgroup-list="alertgroupList"
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
      <template v-if="alertgroupList===null && total<=0">
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
  import mCreateWarningInstance from './_source/createWarningInstance'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'warning-instance-index',
    data () {
      return {
        total: null,
        isLoading: false,
        alertgroupList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
        },
        isLeft: true,
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER',
        createWarningDialog: false,
        item: {},
        pulginInstance: []
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('security', ['queryAlertPluginInstanceListPaging', 'getPlugins']),
      /**
       * Inquire
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
      _onUpdate () {
        this._debounceGET()
      },
      _onEdit (item) {
        this._create(item)
      },
      _create (item) {
        this.getPlugins({ pluginType: 'ALERT' }).then(res => {
          this.pulginInstance = res
        }).catch(e => {
          this.$message.error(e.msg)
        })
        this.item = item
        this.createWarningDialog = true
      },

      onUpdate () {
        this._debounceGET('false')
        this.createWarningDialog = false
      },

      close () {
        this.createWarningDialog = false
      },

      _getList (flag) {
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.queryAlertPluginInstanceListPaging(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.alertgroupList = []
            this.alertgroupList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
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
    created () {
    },
    mounted () {
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mList, mListConstruction, mConditions, mSpin, mNoData, mCreateWarningInstance }
  }
</script>
