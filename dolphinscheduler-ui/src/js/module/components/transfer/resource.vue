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
  <m-popup :ok-text="$t('Submit')" :nameText="type.name + $t('Authorize')" @ok="_ok" ref="popup">
    <template slot="content">
      <div class="clearfix transfer-model" style="width: 660px">
        <div>
            <x-button-group v-model="checkedValue" size="small">
                <x-button type="ghost" value="fileResource" @click="_ckFile">{{$t('File resources')}}</x-button>
                <x-button type="ghost" value="udfResource" @click="_ckUDf">{{$t('UDF resources')}}</x-button>
            </x-button-group>
        </div>
        <treeselect v-show="checkedValue=='fileResource'" v-model="selectFileSource" :multiple="true" :options="fileList" :normalizer="normalizer" :value-consists-of="valueConsistsOf" :placeholder="$t('Please select resources')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
        <treeselect v-show="checkedValue=='udfResource'" v-model="selectUdfSource" :multiple="true" :options="udfList" :normalizer="normalizer" :value-consists-of="valueConsistsOf" :placeholder="$t('Please select resources')">
          <div slot="value-label" slot-scope="{ node }">{{ node.raw.fullName }}</div>
        </treeselect>
        <!-- <div class="select-list-box">
          <div class="tf-header">
            <div class="title">{{type.name}}{{$t('List')}}</div>
            <div class="count">（{{cacheSourceList.length}}）</div>
          </div>
          <div class="scrollbar tf-content">
            <ul>
              <li v-for="(item,$index) in sourceList" :key="$index" @click="_ckSource(item)">
                <span :title="item.name">{{item.name}}</span>
                <a href="javascript:"></a>
              </li>
            </ul>
          </div>
        </div>
        <div class="select-oper-box">&nbsp;</div>
        <div class="select-list-box">
          <div class="tf-header">
            <div class="title">{{$t('Selected')}}{{type.name}}</div>
            <div class="count">（{{cacheTargetList.length}}）</div>
          </div>
          <div class="scrollbar tf-content">
            <ul>
              <li v-for="(item,$index) in targetList" :key="$index" @click="_ckTarget(item)"><span :title="item.name">{{item.name}}</span></li>
            </ul>
          </div>
        </div> -->
      </div>
    </template>
  </m-popup>
</template>
<script>
  import _ from 'lodash'
  import mPopup from '@/module/components/popup/popup'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'


  export default {
    name: 'transfer',
    data () {
      return {
        valueConsistsOf: 'LEAF_PRIORITY',
        checkedValue: 'fileResource',
        sourceList: this.fileSourceList,
        targetList: this.fileTargetList,
        cacheSourceList: this.fileSourceList,
        cacheTargetList: this.fileTargetList,

        fileSource: this.fileSourceList,
        fileList: [],
        udfList: [],
        selectFileSource: [],
        selectUdfSource: [],
        fileTarget: this.fileTargetList,
        udfSource: this.udfSourceList,
        udfTarget: this.udfTargetList,
        searchSourceVal: '',
        searchTargetVal: '',
        // define default value
        value: null,
        normalizer(node) {
          return {
            label: node.name
          }
        },
      }
    },
    props: {
      type: Object,
      fileSourceList: Array,
      udfSourceList: Array,
      fileTargetList: Array,
      udfTargetList: Array,
    },
    created() {
      let file = this.fileSourceList
      let udf = this.udfSourceList
      this.diGuiTree(file)
      this.diGuiTree(udf)
      this.fileList = file
      this.udfList = udf
      this.selectFileSource = this.fileTargetList
      this.selectUdfSource = this.udfTargetList
    },
    methods: {
      /*
        getParent
      */
      getParent(data2, nodeId2) {
        var arrRes = [];
        if (data2.length == 0) {
            if (!!nodeId2) {
                arrRes.unshift(data2)
            }
            return arrRes;
        }
        let rev = (data, nodeId) => {
            for (var i = 0, length = data.length; i < length; i++) {
                let node = data[i];
                if (node.id == nodeId) {
                    arrRes.unshift(node)
                    rev(data2, node.pid);
                    break;
                }
                else {
                    if (!!node.children) {
                        rev(node.children, nodeId);
                    }
                }
            }
            return arrRes;
        };
        arrRes = rev(data2, nodeId2);
        return arrRes;
      },
      _ok () {
        let fullPathId = []
        let pathId = []
        this.selectFileSource.forEach(v=>{
          this.fileList.forEach(v1=>{
            let arr = []
            arr[0] = v1
            if(this.getParent(arr, v).length>0) {
              fullPathId = this.getParent(arr, v).map(v2=>{
                return v2.id
              })
              pathId.push(fullPathId.join('-'))
            }
          })
        })
        let fullUdfPathId = []
        let pathUdfId = []
        this.selectUdfSource.forEach(v=>{
          this.udfList.forEach(v1=>{
            let arr = []
            arr[0] = v1
            if(this.getParent(arr, v).length>0) {
              fullUdfPathId = this.getParent(arr, v).map(v2=>{
                return v2.id
              })
              pathUdfId.push(fullUdfPathId.join('-'))
            }
          })
        })
        let selAllSource = pathId.concat(pathUdfId)
        this.$refs['popup'].spinnerLoading = true
        setTimeout(() => {
          this.$refs['popup'].spinnerLoading = false
          this.$emit('onUpdate', _.map(selAllSource, v => v).join(','))
        }, 800)
      },
      _ckFile() {
        this.checkedValue = "fileResource"
        this.sourceList = this.fileSource
        this.targetList = this.fileTarget
        this.cacheSourceList = this.fileSource
        this.cacheTargetList = this.fileTarget
      },
      _ckUDf() {
        this.checkedValue = "udfResource"
        this.sourceList = this.udfSource
        this.targetList = this.udfTarget
        this.cacheSourceList = this.udfSource
        this.cacheTargetList = this.udfTarget
      },
      _sourceQuery () {
        this.sourceList = this.sourceList.filter(v => v.name.indexOf(this.searchSourceVal) > -1)
      },
      _targetQuery () {
        this.targetList = this.targetList.filter(v => v.name.indexOf(this.searchTargetVal) > -1)
      },
      _ckSource (item) {
        this.targetList = this.cacheTargetList
        this.targetList.unshift(item)
        this.searchTargetVal = ''
        let i1 = _.findIndex(this.sourceList, v => item.id === v.id)
        this.sourceList.splice(i1, 1)
        let i2 = _.findIndex(this.cacheSourceList, v => item.id === v.id)
        if (i2 !== -1) {
          this.cacheSourceList.splice(i2, 1)
        }
        if(this.checkedValue == "fileResource") {
            this.fileTarget = this.targetList
            this.fileSource = this.sourceList
        } else {
            this.udfTarget = this.targetList
            this.udfSource = this.sourceList
        }
      },
      _ckTarget (item) {
        this.sourceList = this.cacheSourceList
        this.sourceList.unshift(item)
        this.searchSourceVal = ''
        let i1 = _.findIndex(this.targetList, v => item.id === v.id)
        this.targetList.splice(i1, 1)
        let i2 = _.findIndex(this.cacheTargetList, v => item.id === v.id)
        if (i2 !== -1) {
          this.cacheTargetList.splice(i2, 1)
        }
        if(this.checkedValue == "fileResource") {
            this.fileSource = this.sourceList
            this.fileTarget = this.targetList
        } else {
            this.udfSource = this.sourceList
            this.udfTarget = this.targetList
        }
      },
      diGuiTree(item) {  // Recursive convenience tree structure
        item.forEach(item => {
          item.children === '' || item.children === undefined || item.children === null || item.children.length === 0?　　　　　　　　
            this.operationTree(item): this.diGuiTree(item.children);
        })
      },
      operationTree(item) {
        if(item.dirctory) {
          item.isDisabled =true
        }
        delete item.children
      }
    },
    watch: {
      searchSourceVal (val) {
        if (!val) {
          this.sourceList = _.cloneDeep(this.cacheSourceList)
          return
        }
        this._sourceQuery()
      },
      searchTargetVal (val) {
        if (!val) {
          this.targetList = _.cloneDeep(this.cacheTargetList)
          return
        }
        this._targetQuery()
      }
    },
    components: { mPopup, mListBoxF, Treeselect }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .transfer-model {
    padding: 0 20px;
    .select-list-box {
      width: 300px;
      float: left;
      border: 1px solid #dcdee2;
      border-radius: 3px;
      .tf-header {
        height: 36px;
        line-height: 36px;
        background: #f9fafc;
        position: relative;
        border-bottom: 1px solid #dcdee2;
        margin-bottom: 8px;
        .title {
          position: absolute;
          left: 8px;
          top: 0;
        }
        .count {
          position: absolute;
          right: 8px;
          top: 0;
          font-size: 12px;
        }
      }
      .tf-search {
        background: #fff;
        padding: 8px;
        .fa-search {
          color: #999;
        }
      }
      .tf-content {
        height: 280px;
        ul {
          li {
            height: 28px;
            line-height: 28px;
            cursor: pointer;
            span {
              padding-left: 10px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              width: 290px;
              display: inline-block;
            }
            &:hover {
              background: #f6faff;
            }
          }
        }
      }
    }
    .select-oper-box {
      width: 20px;
      float: left;
    }
  }
</style>
