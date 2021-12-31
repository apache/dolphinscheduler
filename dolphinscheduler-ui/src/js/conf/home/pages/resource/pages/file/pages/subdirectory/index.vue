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
  <div class="home-main list-construction-model">
    <div class="content-title">
      <a class="bread" style="padding-left: 15px;" @click="() => $router.push({path: `/resource/file`})">{{$t('File Manage')}}</a>
      <a class="bread" v-for="(item,$index) in breadList" :key="$index" @click="_ckOperation(item, $index)">{{'>'+item}}</a>
    </div>
    <div class="conditions-box">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <el-button-group>
            <el-button  size="mini" @click="() => $router.push({path: `/resource/file/subFileFolder/${searchParams.id}`})">{{$t('Create folder')}}</el-button>
            <el-button size="mini" @click="() => $router.push({path: `/resource/file/subFile/${searchParams.id}`})">{{$t('Create File')}}</el-button>
            <el-button size="mini" @click="_uploading">{{$t('Upload Files')}}</el-button>
          </el-button-group>
        </template>
      </m-conditions>
    </div>
    <div class="list-box">
      <template v-if="fileResourcesList.length || total>0">
        <m-list @on-update="_onUpdate" @on-updateList="_updateList" :file-resources-list="fileResourcesList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize">
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
      <template v-if="!fileResourcesList.length && total<=0">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" :is-left="isLeft">
      </m-spin>
   </div>
   </div>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mList from './_source/list'
  import localStore from '@/module/util/localStorage'
  import mSpin from '@/module/components/spin/spin'
  import { findComponentDownward } from '@/module/util/'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'

  export default {
    name: 'resource-list-index-FILE',
    data () {
      return {
        total: null,
        isLoading: false,
        fileResourcesList: [],
        searchParams: {
          id: this.$route.params.id,
          pageSize: 10,
          pageNo: 1,
          searchVal: '',
          type: 'FILE'
        },
        isLeft: true,
        breadList: []
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('resource', ['getResourcesListP', 'getResourceId']),
      /**
       * File Upload
       */
      _uploading () {
        findComponentDownward(this.$root, 'roof-nav')._fileChildUpdate('FILE', this.searchParams.id)
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
        if (sessionStorage.getItem('isLeft') === 0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.searchParams.id = this.$route.params.id
        this.getResourcesListP(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.fileResourcesList = res.totalList
            this.total = res.total
            this.isLoading = false
          }
        }).catch(e => {
          this.isLoading = false
        })
      },
      _updateList (data) {
        this.searchParams.id = data
        this.searchParams.pageNo = 1
        this.searchParams.searchVal = ''
        this._debounceGET()
      },
      _onUpdate () {
        this.searchParams.id = this.$route.params.id
        this._debounceGET()
      },
      _ckOperation (item, index) {
        let breadName = ''
        this.breadList.forEach((item, i) => {
          if (i <= index) {
            breadName = breadName + '/' + item
          }
        })
        this.transferApi(breadName)
      },
      transferApi (api) {
        this.getResourceId({
          type: 'FILE',
          fullName: api,
          id: this.searchParams.id
        }).then(res => {
          localStore.setItem('currentDir', `${res.fullName}`)
          this.$router.push({ path: `/resource/file/subdirectory/${res.id}` })
        }).catch(e => {
          this.$message.error(e.msg || '')
        })
      }
    },
    watch: {
      // router
      '$route' (a) {
        // url no params get instance list
        this.searchParams.pageNo = _.isEmpty(a.query) ? 1 : a.query.pageNo
        this.searchParams.id = a.params.id
        let dir = localStore.getItem('currentDir').split('/')
        dir.shift()
        this.breadList = dir
      }
    },
    created () {},
    mounted () {
      let dir = localStore.getItem('currentDir').split('/')
      dir.shift()
      this.breadList = dir
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft', 1)
    },
    components: { mConditions, mList, mSpin, mNoData }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  .bread {
    font-size: 22px;
    padding-top: 10px;
    color: #2a455b;
    display: inline-block;
    cursor: pointer;
  }
</style>
