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
<div class="wrap-result">
  <m-list-construction :title="$t('DataQuality Rule')">
    <template slot="conditions">
        <m-rule-conditions @on-query="_onQuery"></m-rule-conditions>
    </template>
    <template slot="content">
      <template v-if="ruleList.length || total>0">
        <m-list @on-edit="_onEdit"
                :rule-list="ruleList"
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
      <template v-if="!ruleList.length && total<=0">
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
  import mNoData from '@/module/components/noData/noData'
  import listUrlParamHandle from '@/module/mixin/listUrlParamHandle'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  import mRuleConditions from '@/conf/home/pages/dataquality/_source/conditions/rule'

  export default {
    name: 'data-quality-rule-index',
    data () {
      return {
        total: null,
        isLoading: true,
        ruleList: [],
        searchParams: {
          pageSize: 10,
          pageNo: 1,
          // start date
          startDate: '',
          // end date
          endDate: '',
          // search value
          searchVal: ''
        },
        isLeft: true,
        item: {}
      }
    },
    mixins: [listUrlParamHandle],
    props: {},
    methods: {
      ...mapActions('dataquality', ['getDataQualityRuleListPage']),
      /**
       * click query
       */
      _onQuery (o) {
        this.searchParams = _.assign(this.searchParams, o)
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
        this.getDataQualityRuleListPage(this.searchParams).then(res => {
          if (this.searchParams.pageNo > 1 && res.totalList.length === 0) {
            this.searchParams.pageNo = this.searchParams.pageNo - 1
          } else {
            this.ruleList = []
            res.totalList.forEach((item, i) => {
              if (item.name.indexOf('$t') !== -1) {
                if (item.name) {
                  item.name = this.$t((item.name).replace('$t(', '').replace(')', ''))
                }
              }
              this.ruleList.push(item)
            })
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
    components: { mList, mListConstruction, mSpin, mNoData, mRuleConditions }
  }
</script>
<style lang="scss" rel="stylesheet/scss">
  .wrap-result {
    .table-box {
      overflow-y: scroll;
    }
    .table-box {
      .fixed {
        table-layout: auto;
        tr {
          th:last-child,td:last-child {
            background: inherit;
            width: 60px;
            height: 40px;
            line-height: 40px;
            border-left:1px solid #ecf3ff;
            position: absolute;
            right: 0;
            z-index: 2;
          }
          td:last-child {
            border-bottom:1px solid #ecf3ff;
          }
          th:nth-last-child(2) {
            padding-right: 90px;
          }
        }
      }
    }
    .list-model {
      .el-dialog__header, .el-dialog__body {
        padding: 0;
      }
    }
  }
 </style>
