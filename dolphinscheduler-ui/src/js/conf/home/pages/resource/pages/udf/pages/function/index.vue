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
  <m-list-construction :title="$t('UDF Function')">
    <template slot="conditions">
      <m-conditions @on-conditions="_onConditions">
        <template slot="button-group">
          <x-button-group size="small">
            <x-button type="ghost" @click="_create">{{$t('Create UDF Function')}}</x-button>
          </x-button-group>
        </template>
      </m-conditions>
    </template>
    <template slot="content">
      <template v-if="udfFuncList.length || total>0">
        <m-list :udf-func-list="udfFuncList" :page-no="searchParams.pageNo" :page-size="searchParams.pageSize" @on-update="_updateList">
        </m-list>
        <div class="page-box">
          <x-page :current="parseInt(searchParams.pageNo)" :total="total" :page-size="searchParams.pageSize" show-elevator @on-change="_page" show-sizer :page-size-options="[10,30,50]" @on-size-change="_pageSize"></x-page>
        </div>
      </template>
      <template v-if="!udfFuncList.length && total<=0">
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
  import mCreateUdf from './_source/createUdf'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mConditions from '@/module/components/conditions/conditions'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'udf-function-index',
    data () {
      return {
        total: null,
        isLoading: false,
        udfFuncList: [],
        searchParams: {
          id: -1,
          pageSize: 10,
          pageNo: 1,
          searchVal: ''
        },
        isLeft: true
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('resource', ['getUdfFuncListP']),
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
      _create () {
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateUdf, {
              on: {
                onUpdate () {
                  self._updateList()
                  modal.remove()
                },
                close () {
                  modal.remove()
                }
              },
              props: {
              }
            })
          }
        })
      },
      _updateList () {
        this._debounceGET()
      },
      _getList (flag) {
        if(sessionStorage.getItem('isLeft')==0) {
          this.isLeft = false
        } else {
          this.isLeft = true
        }
        this.isLoading = !flag
        this.getUdfFuncListP(this.searchParams).then(res => {
          if(this.searchParams.pageNo>1 && res.totalList.length == 0) {
            this.searchParams.pageNo = this.searchParams.pageNo -1
          } else {
            this.udfFuncList = []
            this.udfFuncList = res.totalList
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
      this.$modal.destroy()
    },
    beforeDestroy () {
      sessionStorage.setItem('isLeft',1)
    },
    components: { mListConstruction, mConditions, mList, mSpin, mCreateUdf, mNoData }
  }
</script>
