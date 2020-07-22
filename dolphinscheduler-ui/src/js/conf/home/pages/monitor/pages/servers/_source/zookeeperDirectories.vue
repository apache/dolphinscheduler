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
  <div class="container">
    <div class="title-box">
      <span class="name">{{$t('zkDirectory')}}</span>
    </div>

    <div class="table-box" v-if="zkDirectories.length > 0">
      <table class="fixed">
        <caption><!-- placeHolder --></caption>
        <tr>
          <th scope="col" style="min-width: 40px">
            <span>#</span>
          </th>
          <th scope="col" style="min-width: 40px">
            <span>{{$t('zkDirectory')}}</span>
          </th>
        </tr>
        <tr v-for="(item, $index) in zkDirectories" :key="item.id">
          <td>
            <span>{{$index + 1}}</span>
          </td>
          <td>
            <span>{{item.zkDirectory}}</span>
          </td>
        </tr>
      </table>
    </div>

    <div v-if="zkDirectories.length === 0">
      <m-no-data><!----></m-no-data>
    </div>

    <div v-if="zkDirectories.length > 0">
      <div class="bottom-box">
      </div>
    </div>
  </div>
</template>

<script>
  import mNoData from '@/module/components/noData/noData'

  export default {
    name: 'zookeeperDirectoriesPopup',
    data () {
      return {
        tableHeaders: [
          {
            label: $t('zkDirectory'),
            prop: 'zkDirectory'
          }
        ]
      }
    },
    props: {
      zkDirectories: Array
    },
    components: { mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .container {
    width: 500px;
    .title-box {
      height: 61px;
      border-bottom: 1px solid #DCDEDC;
      position: relative;
      .name {
        position: absolute;
        left: 24px;
        top: 18px;
        font-size: 16px;
      }
    }
    .bottom-box {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      text-align: right;
      height: 60px;
      line-height: 60px;
      border-top: 1px solid #DCDEDC;
      background: #fff;
      .ans-page {
        display: inline-block;
      }
    }
    .table-box {
      overflow-y: scroll;
      height: calc(100vh - 61px);
      padding-bottom: 60px;
    }
  }
</style>
