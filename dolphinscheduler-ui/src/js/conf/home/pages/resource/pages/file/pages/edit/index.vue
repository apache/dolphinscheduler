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
  <m-list-construction :title="$t('File Details')">
    <div slot="content" style="margin: 20px">
      <div class="file-edit-content">
        <h2>
          <span>{{name}}</span>
        </h2>
        <template v-show="isNoType">
          <template v-if="!msg">
            <div class="code-mirror-model">
              <textarea id="code-edit-mirror" name="code-edit-mirror"></textarea>
            </div>
            <div class="submit-c">
              <x-button type="text" shape="circle" @click="close()" :disabled="disabled"> {{$t('Return')}} </x-button>
              <x-button type="primary" shape="circle" :loading="spinnerLoading" @click="ok()">{{spinnerLoading ? 'Loading...' : $t('Save')}} </x-button>
            </div>
          </template>
          <m-no-data :msg="msg" v-if="msg"></m-no-data>

        </template>
        <template v-if="!isNoType">
          <m-no-type></m-no-type>
        </template>
      </div>
      <m-spin :is-spin="isLoading">
      </m-spin>
    </div>
  </m-list-construction>
</template>
<script>
  import i18n from '@/module/i18n'
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import { filtTypeArr } from '../_source/common'
  import mNoType from '../details/_source/noType'
  import { bytesToSize } from '@/module/util/util'
  import codemirror from '../_source/codemirror'
  import mSpin from '@/module/components/spin/spin'
  import localStore from '@/module/util/localStorage'
  import mNoData from '@/module/components/noData/noData'
  import { handlerSuffix } from '../details/_source/utils'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  let editor

  export default {
    name: 'file-details',
    data () {
      return {
        name: '',
        isNoType: true,
        isLoading: false,
        filtTypeArr: filtTypeArr,
        loadingIndex: 0,
        mode: 'python',
        isData: true,
        size: null,
        spinnerLoading: false,
        msg: ''
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['getViewResources', 'updateContent']),
      ok () {
        if (this._validation()) {
            this.spinnerLoading = true
            this.updateContent({
            id: this.$route.params.id,
            content: editor.getValue()
          }).then(res => {
            this.$message.success(res.msg)
            setTimeout(() => {
              this.spinnerLoading = false
              this.close()
            }, 800)
          }).catch(e => {
            this.$message.error(e.msg || '')
            this.spinnerLoading = false
          })
        }
      },
      _validation () {
        if (editor.doc.size>3000) {
          this.$message.warning(`${i18n.$t('Resource content cannot exceed 3000 lines')}`)
          return false
        }
        return true
      },
      close () {
        this.$router.go(-1)
      },
      _getViewResources () {
        this.isLoading = true
        this.getViewResources({
          id: this.$route.params.id,
          skipLineNum: 0,
          limit: 3000
        }).then(res => {
          this.name = res.data.alias.split('.')[0]
          if (!res.data) {
            this.isData = false
          } else {
            this.isData = true
            let content = res.data.content ? res.data.content + '\n' : ''
            this._handlerEditor().setValue(content)
            setTimeout(() => {
              $('.code-mirror-model').scrollTop(12).scrollLeft(0)
            }, 200)
          }
          this.isLoading = false
        }).catch(e => {
          this.msg = e.msg || 'error'
          this.$message.error(e.msg || '')
          this.isLoading = false
        })
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // editor
        editor = codemirror('code-edit-mirror', {
          mode: this.mode,
          readOnly: false
        })

        this.keypress = () => {
          if (!editor.getOption('readOnly')) {
            editor.showHint({
              completeSingle: false
            })
          }
        }

        // Monitor keyboard
        editor.on('keypress', this.keypress)

        return editor
      }
    },
    watch: {
    },
    created () {
      let file = _.split(localStore.getItem('file'), '|', 2)
      let fileName = file[0]
      let fileSize = file[1]
      let i = fileName.lastIndexOf('.')
      let a = fileName.substring(i, fileName.length)
      this.mode = handlerSuffix[a]
      this.size = bytesToSize(parseInt(fileSize))
      this.isNoType = _.includes(this.filtTypeArr, _.trimStart(a, '.'))
    },
    mounted () {
      if (this.isNoType) {
        // get data
        this._getViewResources()
      }
    },
    destroyed () {
      if (editor) {
        editor.toTextArea()
        editor.off($('.code-edit-mirror'), 'keypress', this.keypress)
      }
    },
    computed: {
    },
    components: { mListConstruction, mNoType, mSpin, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .file-edit-content {
    width: 100%;
    background: #fff;
    padding-bottom: 20px;
    >h2 {
      line-height: 60px;
      text-align: center;
      padding-bottom: 6px;
      position: relative;
      .down {
        position: absolute;
        right: 0;
        top: 0;
        >i {
          font-size: 20px;
          color: #2d8cf0;
          cursor: pointer;
          vertical-align: middle;
        }
        em {
          font-size: 12px;
          font-style: normal;
          vertical-align: middle;
          color: #777;
          margin-left: -2px;
        }
      }
    }
    .code-mirror-model {
      height: calc(100vh - 300px);
      .cm-s-mdn-like.CodeMirror {
        height: calc(100vh - 310px);
      }
    }

    .submit-c {
      text-align: center;
      padding-top: 12px;
    }

  }
  .file-operation {
    padding: 30px 0;
    text-align: center;
  }
</style>
