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
      <div class="file-details-content">
        <h2>
          <span>{{name}}</span>
          <div class="down">
            <em class="el-icon-download" style="font-size:20px" data-container="body" data-toggle="tooltip" :title="$t('Download Details')" @click="_downloadFile"></em>
            <em>{{size}}</em>
            &nbsp;&nbsp;
            <em class="el-icon-circle-close" style="font-size:20px" data-container="body" data-toggle="tooltip" :title="$t('Return')" @click="close()"></em>
            &nbsp;&nbsp;
          </div>
        </h2>
        <template v-if="isViewType">

          <div class="code-mirror-model" v-if="!msg">
            <textarea id="code-details-mirror" name="code-details-mirror"></textarea>
          </div>

          <m-no-data :msg="msg" v-if="msg"></m-no-data>

        </template>
        <template v-if="!isViewType">
          <m-no-type></m-no-type>
        </template>
      </div>
      <m-spin :is-spin="isLoading">
      </m-spin>
    </div>
  </m-list-construction>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import mNoType from './_source/noType'
  import { filtTypeArr } from '../_source/common'
  import codemirror from '../_source/codemirror'
  import { handlerSuffix } from './_source/utils'
  import { downloadFile } from '@/module/download'
  import { bytesToSize } from '@/module/util/util'
  import mSpin from '@/module/components/spin/spin'
  import localStore from '@/module/util/localStorage'
  import mNoData from '@/module/components/noData/noData'
  import mListConstruction from '@/module/components/listConstruction/listConstruction'

  let editor

  export default {
    name: 'file-details',
    data () {
      return {
        name: '',
        isViewType: true,
        isLoading: false,
        filtTypeArr: filtTypeArr,
        loadingIndex: 0,
        mode: 'python',
        isData: true,
        size: null,
        msg: ''
      }
    },
    props: {},
    methods: {
      ...mapActions('resource', ['getViewResources', 'updateContent']),
      _go () {
        this.$router.push({ name: 'file' })
      },
      close () {
        this.$router.go(-1)
      },
      _downloadFile () {
        downloadFile('resources/download', {
          id: this.$route.params.id
        })
      },
      _getViewResources () {
        this.isLoading = true
        this.isData = true
        this.msg = ''
        this.getViewResources(this._rtParam).then(res => {
          this.name = res.data.alias.split('.')[0]
          if (!res.data) {
            this.isData = false
          } else {
            this.isData = true
            this._handlerEditor().setValue(res.data.content + '\n')

            // Initialize the plugin to prevent repeated calls
            if (editor.lineCount() < 1000) {
              this.isData = false
            }

            setTimeout(() => {
              $('.code-mirror-model').scrollTop(12).scrollLeft(0)
            }, 200)
          }
          this.isLoading = false
        }).catch(e => {
          this.msg = e.msg || 'error'
          this.$message.error(e.msg || '')
          // this._handlerEditor().setValue('')
          this.isLoading = false
        })
      },
      /**
       * up
       */
      _onUp: _.debounce(function () {
        this.loadingIndex = this.loadingIndex - 3
        console.log('_onUp')
        this._editorOff()

        this._getViewResources()
      }, 1000, {
        leading: false,
        trailing: true
      }),
      /**
       * down
       */
      _onDown: _.debounce(function () {
        this.loadingIndex = this.loadingIndex + 3
        console.log('_onDown')
        this._editorOff()

        this._getViewResources()
      }, 1000, {
        leading: false,
        trailing: true
      }),
      /**
       * off handle
       */
      _editorOff () {
        editor = null
        $('.CodeMirror').remove()
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        let h = $('.home-main').height() - 160

        editor = codemirror('code-details-mirror', {
          mode: this.mode
        })

        editor.display.wrapper.style.height = `${h}px`

        this.scroll = ({ doc }) => {
          let scrollTop = doc.scrollTop
          let totalHeight = doc.height

          // down
          if ((scrollTop + h) > totalHeight) {
            if (this.isData) {
              // this._onDown()
            }
          }
          // up
          if (scrollTop < 3) {
            if (this.loadingIndex > 0) {
              this._onUp()
            }
          }
        }

        editor.on('scroll', this.scroll)
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
      this.isViewType = _.includes(this.filtTypeArr, _.trimStart(a, '.'))
    },
    mounted () {
      if (this.isViewType) {
        // get data
        this._getViewResources()
      }
    },
    destroyed () {
      if (editor) {
        editor.toTextArea()
        editor.off($('.code-details-mirror'), 'scroll', this.scroll)
      }
    },
    computed: {
      _rtParam () {
        return {
          id: this.$route.params.id,
          skipLineNum: parseInt(`${this.loadingIndex ? this.loadingIndex + '000' : 0}`),
          limit: parseInt(`${this.loadingIndex ? this.loadingIndex + 3 : 3}000`)
        }
      }
    },
    components: { mListConstruction, mNoType, mSpin, mNoData }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .file-details-content {
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
        >em {
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
  }
  .file-operation {
    padding: 30px 0;
    text-align: center;
  }
</style>
