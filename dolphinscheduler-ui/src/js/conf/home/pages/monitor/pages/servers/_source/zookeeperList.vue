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
      <table>
        <tr>
          <th>
            <span>{{$t('#')}}</span>
          </th>
          <th>
            <span>{{$t('host')}}</span>
          </th>
          <th>
            <span>{{$t('Number of connections')}}</span>
          </th>
          <th>
            <span>watches {{$t('Number')}}</span>
          </th>
          <th>
            <span>{{$t('Sent')}}</span>
          </th>
          <th>
            <span>{{$t('Received')}}</span>
          </th>
          <th>
            <span>leader/follower</span>
          </th>
          <th>
            <span>{{$t('Min latency')}}</span>
          </th>
          <th>
            <span>{{$t('Avg latency')}}</span>
          </th>
          <th>
            <span>{{$t('Max latency')}}</span>
          </th>
          <th>
            <span>{{$t('Node count')}}</span>
          </th>
          <th>
            <span>{{$t('Query time')}}</span>
          </th>
          <th style="text-align: center">
            <span>{{$t('Node self-test status')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in list" :key="$index" @mouseenter="_showErrorMessage(item)">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span>
              {{item.hostname}}
            </span>
          </td>
          <td><span>{{item.connections}}</span></td>
          <td>
            <span>{{item.watches}}</span>
          </td>
          <td>
            <span>{{item.sent}}</span>
          </td>
          <td>
            <span>{{item.received}}</span>
          </td>
          <td><span>{{item.mode}}</span></td>
          <td>
            <span>{{item.minLatency}}</span>
          </td>
          <td>
            <span>{{item.avgLatency}}</span>
          </td>
          <td>
            <span>{{item.maxLatency}}</span>
          </td>
          <td>
            <span>{{item.nodeCount}}</span>
          </td>
          <td>
            <span v-if="item.date">{{item.date | formatDate}}</span>
            <span v-else>-</span>
          </td>
          <td>
            <span class="state">
              <em class="ans-icon-success-solid success" v-if="item.state === 1"></em>
              <em class="ans-icon-warn-solid warn" v-else-if="item.state === 0"></em>
              <em class="ans-icon-fail-solid error" v-else></em>
            </span>
          </td>
        </tr>
      </table>
    </div>
  </div>
</template>
<script>

  import _ from 'lodash'
  import i18n from '@/module/i18n'

  export default {
    name: 'zookeeper-list',
    data () {
      return {
        list: []
      }
    },
    props: {
      list: Array
    },
    methods:{
      _showErrorMessage:_.debounce(function (item){
        const hostname = item.hostname
        const state = item.state
        if(state === -1){
          this.$message.error(`${i18n.$t('Can not connect to zookeeper server:')}`+hostname)
        }
      },600)
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
      .warn {
              color: #fabc05;
      }
      .error {
        color: #ff0000;
      }
    }
  }
</style>

