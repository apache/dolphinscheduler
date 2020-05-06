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
  <m-list-construction :title="$t('File Manage')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button-group size="small" >
            <x-button type="ghost" @click="() => $router.push({name: 'resource-file-createFolder'})">{{$t('Create folder')}}</x-button>
            <x-button type="ghost" @click="() => $router.push({name: 'resource-file-create'})">{{$t('Create File')}}</x-button>
            <x-button type="ghost" @click="_uploading">{{$t('Upload Files')}}</x-button>
          </x-button-group>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="fileResourcesList.length || total>0">
        <m-list @on-update="_onUpdate" :file-resources-list="fileResourcesList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
        </m-list>
        <div class="page-box">
          <x-page :current="parseInt(searchParams.pageNo)" :total="total" :page-size="searchParams.pageSize" show-elevator @on-change="_page" show-sizer :page-size-options="[10,30,50]" @on-size-change="_pageSize"></x-page>
        </div>
      </template>
      <template v-if="!fileResourcesList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="isLeft">
      </m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import mSpin from '@/module/components/spin/spin'
  import { findComponentDownward } from '@/module/util/'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'resource-list-index-FILE',
    data () {
      return {
        total: null,
        isLoading: false,
        fileResourcesList: [],
        searchParams: {
          id: -1,
          pageSize: 10,
          pageNo: 1,
          searchVal: '',
          type: 'FILE'
        },
        isLeft: true
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('resource', ['getResourcesListP']),
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileUpdate('FILE')
      },
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
      _getList (flag) {
        if(sessionStorage.getItem('isLeft')==0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.getResourcesListP(this.searchParams).then(res => {
          if(this.searchParams.pageNo>1 && res.totalList.length == 0) {
            this.searchParams.pageNo = this.searchParams.pageNo -1
          } else {
            this.fileResourcesList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
        }).catch(e => {
          this.isLoading = false
        })
      },
      _updateList () {
        this.searchParams.pageNo = 1
        this.searchParams.searchVal = ''
        this._debounceGET()
      },
       _onUpdate () {
        this._debounceGET()
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
      this.$modal.destroy()
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft',1)
    },
    components: { mListConstruction, mConditions, mList, mSpin, mNoData }
  }
</script>
