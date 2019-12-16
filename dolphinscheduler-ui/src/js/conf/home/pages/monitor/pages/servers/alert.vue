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
  <m-list-construction title="Alert Service">
    <template slot="content">
      <template v-if="masterList.length">
        <m-list :list="masterList"></m-list>
      </template>
      <template v-if="!masterList.length">
        <m-no-data></m-no-data>
      </template>
      <m-spin :is-spin="isLoading" ></m-spin>
    </template>
  </m-list-construction>
</template>
<script>
  import { mapActions } from 'vuex'
  import mList from './_source/zookeeperList'
  import mSpin from '@/module/components/spin/spin'
  import mNoData from '@/module/components/noData/noData'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  export default {
    name: 'servers-alert',
    data () {
      return {
        pageSize: 10,
        pageNo: 1,
        totalPage: null,
        searchVal: '',
        isLoading: false,
        masterList: []
      }
    },
    props: {},
    methods: {
      ...mapActions('security', ['getProcessMasterList'])
    },
    watch: {},
    created () {
      this.isLoading = true
      this.getProcessMasterList().then(res => {
        this.masterList = res.data
        this.isLoading = false
      })
    },
    mounted () {
    },
    components: { mList, mListConstruction, mSpin, mNoData }
  }
</script>
