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
  <m-list-construction :title="$t('User Manage')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group" v-if="userList.length">
          <el-button size="mini" @click="_create('')">{{$t('Create User')}}</el-button>
          <el-dialog
            :title="item ? $t('Edit User') : $t('Create User')"
            v-if="createUserDialog"
            :visible.sync="createUserDialog"
            width="auto">
            <m-create-user :item="item" @onUpdate="onUpdate" @close="close"></m-create-user>
          </el-dialog>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="userList.length || total>0">
        <m-list @on-edit="_onEdit"
                @on-update="_onUpdate"
                :user-list="userList"
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
      <template v-if="!userList.length && total<=0">
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
  import mCreateUser from './_source/createUser'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'users-index',
    data () {
      return {
        total: null,
        isLoading: true,
        userList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
        },
        isLeft: true,
        createUserDialog: false,
        item: {}
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('security', ['getUsersListP']),
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
      _onUpdate () {
        this._debounceGET()
      },
      _onEdit (item) {
        this._create(item)
      },
      _create (item) {
        this.item = item
        this.createUserDialog = true
      },

      onUpdate () {
        this._debounceGET('false')
        this.createUserDialog = false
      },

      close () {
        this.createUserDialog = false
      },

      _getList (flag) {
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.getUsersListP(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.userList = []
            this.userList = res.totalList
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
    components: { mList, mListConstruction, mConditions, mSpin, mNoData, mCreateUser }
  }
</script>
