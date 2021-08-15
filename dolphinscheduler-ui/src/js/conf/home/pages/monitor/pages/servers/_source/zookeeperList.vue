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
  <div class="list-model zookeeper-list">
    <div class="table-box">
      <el-table :data="list" size="mini" style="width: 100%">
        <el-table-column type="index" :label="$t('#')" width="50"></el-table-column>
        <el-table-column prop="hostname" :label="$t('host')" min-width="130"></el-table-column>
        <el-table-column prop="connections" :label="$t('Number of connections')"></el-table-column>
        <el-table-column prop="watches" :label="'watches'+$t('Number')"></el-table-column>
        <el-table-column prop="sent" :label="$t('Sent')"></el-table-column>
        <el-table-column prop="received" :label="$t('Received')"></el-table-column>
        <el-table-column prop="mode" label="leader/follower" min-width="100"></el-table-column>
        <el-table-column prop="minLatency" :label="$t('Min latency')"></el-table-column>
        <el-table-column prop="avgLatency" :label="$t('Avg latency')"></el-table-column>
        <el-table-column prop="maxLatency" :label="$t('Max latency')"></el-table-column>
        <el-table-column prop="nodeCount" :label="$t('Node count')"></el-table-column>
        <el-table-column :label="$t('Query time')" min-width="130">
          <template slot-scope="scope">
            <span>{{scope.row.date | formatDate}}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('Node self-test status')" min-width="90">
          <template slot-scope="scope">
            <span class="state">
              <em class="el-icon-success success" v-if="scope.row.state"></em>
              <em class="el-icon-error error" v-else></em>
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
  export default {
    name: 'zookeeper-list',
    props: {
      list: Array
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .zookeeper-list {
    .state {
      text-align: center;
      display: block;
      >i {
        font-size: 18px;
      }
      .success {
        color: #33cc00;
      }
      .error {
        color: #ff0000;
      }
    }
  }
</style>
