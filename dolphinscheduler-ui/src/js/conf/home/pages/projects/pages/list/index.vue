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
  <m-list-construction :title="$t('Project')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <el-button size="mini" @click="_create('')">{{ $t('Create Project') }}</el-button>
          <el-dialog
            :title="item ? $t('Edit') : $t('Create Project')"
            v-if="createProjectDialog"
            :visible.sync="createProjectDialog"
            width="auto">
            <m-create-project :item="item" @_onUpdate="_onUpdate" @close="_close"></m-create-project>
          </el-dialog>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="projectsList.length || total>0">
        <m-list :projects-list="projectsList" @on-update="_onUpdate" :page-no="searchParams.pageNo"
                :page-size="searchParams.pageSize"></m-list>
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
      <template v-if="!projectsList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="false"></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import mCreateProject from './_source/createProject'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'projects-list',
    data () {
      return {
        total: null,
        projectsList: [],
        isLoading: true,
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
        },
        createProjectDialog: false,
        item: {}
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('projects', ['getProjectsList']),
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
      _create (item) {
        this.createProjectDialog = true
        this.item = item
      },
      _onUpdate () {
        this.createProjectDialog = false
        this._debounceGET()
      },
      _close () {
        this.createProjectDialog = false
      },
      _getList (flag) {
        this.isLoading = !flag
        this.getProjectsList(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.projectsList = []
            this.projectsList = res.totalList
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
    components: { mListConstruction, mSpin, mConditions, mList, mCreateProject, mNoData }
  }
</script>
