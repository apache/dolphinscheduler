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
  <m-list-construction :title="$t('Cron Manage')">
    <template slot="operation">
      <span style=" float: right; padding-right:50px">
        <el-tooltip :content="$t('Return')" placement="top">
          <el-button type="primary" icon="el-icon-back" size="mini" @click="_close()"></el-button>
        </el-tooltip>
      </span>
    </template>
    <template slot="content">
      <m-list></m-list>
    </template>
  </m-list-construction>
</template>
<script>
  import mList from './_source/list'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'
  export default {
    name: 'definition-timing-index',
    methods: {
      _close () {
        this.$router.go(-1)
      }
    },
    components: { mList, mListConstruction }
  }
</script>
