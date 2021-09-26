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
  <form-create v-model="$f" :rule="rule" :option="option"></form-create>
</template>

<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import mListBox from '../_source/listBox'
  import mScriptBox from '../_source/scriptBox'
  import mLocalParams from '../_source/localParams'
  import disabledState from '@/module/mixin/disabledState'
  import Treeselect from '@riophae/vue-treeselect'
  import '@riophae/vue-treeselect/dist/vue-treeselect.css'
  import codemirror from '@/conf/home/pages/resource/pages/file/pages/_source/codemirror'
  import Clipboard from 'clipboard'
  import { diGuiTree, searchTree } from '../_source/resourceTree'
  import formCreate from '@form-create/element-ui'

  let editor
  formCreate.component('treeselect', Treeselect)
  formCreate.component('mListBox', mListBox)
  formCreate.component('mLocalParams', mLocalParams)
  formCreate.component('mScriptBox', mScriptBox)

  export default {
    name: 'shell-form-model',
    data () {
      return {
        $f: {},
        valueConsistsOf: 'LEAF_PRIORITY',
        // script
        rawScript: '',
        // Custom parameter
        localParams: [],
        // resource(list)
        resourceList: [],
        // Cache ResourceList
        cacheResourceList: [],
        // resource select options
        resourceOptions: [],
        // define options
        option: {
          submitBtn: false
        },
        rule: [],
        normalizer (node) {
          return {
            label: node.name
          }
        },
        allNoResources: [],
        noRes: [],
        item: '',
        scriptBoxDialog: false
      }
    },
    mixins: [disabledState],
    props: {
      backfillItem: Object
    },
    methods: {
      _initRule () {
        this.rule = [
          {
            type: 'div',
            class: 'shell-model',
            field: 'shell',
            children: [
              {
                type: 'm-list-box',
                field: 'script',
                native: true,
                children: [
                  {
                    type: 'div',
                    slot: 'text',
                    children: [i18n.$t('Script')]
                  },
                  {
                    type: 'div',
                    slot: 'content',
                    children: [
                      {
                        type: 'div',
                        class: 'form-mirror',
                        children: [
                          {
                            type: 'input',
                            name: 'code-shell-mirror',
                            field: 'code',
                            props: {
                              type: 'textarea',
                              id: 'code-shell-mirror'
                            }
                          },
                          {
                            type: 'a',
                            class: 'ans-modal-box-max',
                            children: [
                              {
                                type: 'em',
                                class: 'el-icon-full-screen',
                                on: {
                                  click: this.setEditorVal
                                }
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                type: 'm-list-box',
                field: 'resources',
                native: true,
                children: [
                  {
                    type: 'div',
                    slot: 'text',
                    children: [i18n.$t('Resources')]
                  },
                  {
                    type: 'div',
                    slot: 'content',
                    children: [
                      {
                        type: 'treeselect',
                        field: 'resourceList',
                        native: true,
                        value: this.resourceList,
                        props: {
                          placeholder: i18n.$t('Please select resources'),
                          multiple: 'true',
                          maxHeight: '200',
                          options: this.resourceOptions,
                          normalizer: this.normalizer,
                          disabled: this.isDetails,
                          valueConsistsOf: this.valueConsistsOf
                        },
                        sync: [
                          { options: this.resourceOptions },
                          { valueConsistsOf: this.valueConsistsOf },
                          { disabled: this.isDetails }
                        ],
                        children: [
                          {
                            type: 'div',
                            slot: 'value-label',
                            field: 'node',
                            children: [
                              {
                                type: 'span',
                                class: 'copy-path',
                                on: {
                                  mousedown: ($event, node) =>
                                    this._copyPath($event, node)
                                },
                                children: [
                                  {
                                    type: 'em',
                                    class: 'el-icon-copy-document',
                                    title: i18n.$t('Copy path'),
                                    props: {
                                      dataContainer: 'body',
                                      dataToggle: 'tooltip'
                                    }
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                type: 'm-list-box',
                field: 'cusParams',
                native: true,
                children: [
                  {
                    type: 'div',
                    slot: 'text',
                    children: [i18n.$t('Custom Parameters')]
                  },
                  {
                    type: 'div',
                    slot: 'content',
                    children: [
                      {
                        type: 'm-local-params',
                        field: 'refLocalParams',
                        props: {
                          udpList: this.localParams,
                          hide: true
                        },
                        sync: [{ udpList: this.localParams }],
                        on: {
                          onLocalParams: this._onLocalParams
                        }
                      }
                    ]
                  }
                ]
              },
              {
                type: 'el-dialog',
                props: {
                  visible: this.scriptBoxDialog,
                  appendToBody: true,
                  width: '80%'
                },
                sync: ['visible'],
                children: [
                  {
                    type: 'mScriptBox',
                    props: {
                      item: this.item
                    },
                    sync: [{ item: this.item }],
                    on: {
                      getSriptBoxValue: this.getSriptBoxValue,
                      closeAble: this.closeAble
                    }
                  }
                ]
              }
            ]
          }
        ]
      },
      backfill () {
        let o = this.backfillItem

        // Non-null objects represent backfill
        if (!_.isEmpty(o)) {
          this.rawScript = o.params.rawScript || ''

          // backfill resourceList
          let backResource = o.params.resourceList || []
          let resourceList = o.params.resourceList || []
          if (resourceList.length) {
            _.map(resourceList, v => {
              if (!v.id) {
                this.store.dispatch('dag/getResourceId', {
                  type: 'FILE',
                  fullName: '/' + v.res
                }).then(res => {
                  this.resourceList.push(res.id)
                  this.dataProcess(backResource)
                }).catch(e => {
                  this.resourceList.push(v.res)
                  this.dataProcess(backResource)
                })
              } else {
                this.resourceList.push(v.id)
                this.dataProcess(backResource)
              }
            })
            this.cacheResourceList = resourceList
          }

          // backfill localParams
          let localParams = o.params.localParams || []
          if (localParams.length) {
            this.localParams = localParams
          }
        }
      },
      _copyPath (e, node) {
        console.log(e, node)
        e.stopPropagation()
        let clipboard = new Clipboard('.copy-path', {
          text: function () {
            return node.raw.fullName
          }
        })
        clipboard.on('success', (handler) => {
          this.$message.success(`${i18n.$t('Copy success')}`)
          // Free memory
          clipboard.destroy()
        })
        clipboard.on('error', (handler) => {
          // Copy is not supported
          this.$message.warning(
            `${i18n.$t('The browser does not support automatic copying')}`
          )
          // Free memory
          clipboard.destroy()
        })
      },
      /**
       * return localParams
       */
      _onLocalParams (a) {
        this.localParams = a
      },
      setEditorVal () {
        this.item = editor.getValue()
        this.scriptBoxDialog = true
      },
      getSriptBoxValue (val) {
        editor.setValue(val)
      // this.scriptBoxDialog = false
      },
      closeAble () {
      // this.scriptBoxDialog = false
      },
      /**
       * return resourceList
       *
       */
      _onResourcesData (a) {
        this.$f.form.resourceList = a
      },
      /**
       * cache resourceList
       */
      _onCacheResourcesData (a) {
        this.cacheResourceList = a
      },
      /**
       * verification
       */
      _shellVerification () {
        // rawScript verification
        if (!editor.getValue()) {
          this.$message.warning(`${i18n.$t('Please enter script(required)')}`)
          return false
        }

        // localParams Subcomponent verification
        if (!this.$f.el('refLocalParams')._verifProp()) {
          return false
        }
        // noRes
        if (this.noRes.length > 0) {
          this.$message.warning(
            `${i18n.$t('Please delete all non-existent resources')}`
          )
          return false
        }
        // Process resourcelist
        let dataProcessing = _.map(this.$f.form.resourceList, (v) => {
          return {
            id: v
          }
        })
        // storage
        this.params = {
          resourceList: dataProcessing,
          localParams: this.localParams,
          rawScript: editor.getValue()
        }
        return true
      },
      /**
       * Processing code highlighting
       */
      _handlerEditor () {
        // editor
        editor = codemirror('code-shell-mirror', {
          mode: 'shell',
          readOnly: this.isDetails
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
        editor.setValue(this.rawScript)

        return editor
      },
      dataProcess (backResource) {
        let isResourceId = []
        let resourceIdArr = []
        if (this.$f.form.resourceList.length > 0) {
          this.$f.form.resourceList.forEach((v) => {
            this.resourceOptions.forEach((v1) => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map((item) => {
            return item.id
          })
          Array.prototype.diff = function (a) {
            return this.filter(function (i) {
              return a.indexOf(i) < 0
            })
          }
          let diffSet = this.$f.form.resourceList.diff(resourceIdArr)
          let optionsCmp = []
          if (diffSet.length > 0) {
            diffSet.forEach((item) => {
              backResource.forEach((item1) => {
                if (item === item1.id || item === item1.res) {
                  optionsCmp.push(item1)
                }
              })
            })
          }
          let noResources = [
            {
              id: -1,
              name: $t('Unauthorized or deleted resources'),
              fullName: '/' + $t('Unauthorized or deleted resources'),
              children: []
            }
          ]
          if (optionsCmp.length > 0) {
            this.allNoResources = optionsCmp
            optionsCmp = optionsCmp.map((item) => {
              return { id: item.id, name: item.name, fullName: item.res }
            })
            optionsCmp.forEach((item) => {
              item.isNew = true
            })
            noResources[0].children = optionsCmp
            this.resourceOptions = this.resourceOptions.concat(noResources)
          }
        }
      }
    },
    watch: {
      // Watch the cacheParams
      cacheParams (val) {
        this.params = Object.assign(this.params, {}, val)
        this._cacheItem()
      },
      resourceIdArr (arr) {
        let result = []
        arr.forEach((item) => {
          this.allNoResources.forEach((item1) => {
            if (item.id === item1.id) {
              // resultBool = true
              result.push(item1)
            }
          })
        })
        this.noRes = result
      }
    },
    computed: {
      resourceIdArr () {
        let isResourceId = []
        let resourceIdArr = []
        if (
          this.$f &&
          this.$f.form.resourceList &&
          this.$f.form.resourceList.length > 0
        ) {
          this.$f.form.resourceList.forEach((v) => {
            this.resourceOptions.forEach((v1) => {
              if (searchTree(v1, v)) {
                isResourceId.push(searchTree(v1, v))
              }
            })
          })
          resourceIdArr = isResourceId.map((item) => {
            return { id: item.id, name: item.name, res: item.fullName }
          })
        }
        return resourceIdArr
      },
      cacheParams () {
        return {
          resourceList: this.resourceIdArr,
          localParams: this.localParams
        }
      }
    },
    created () {
      let item = this.store.state.dag.resourcesListS
      diGuiTree(item)
      this.resourceOptions = item
    },
    mounted () {
      let self = this
      this._initRule()
      setTimeout(() => {
        window.$$ = this
        $('#cancelBtn').mousedown(function (event) {
          event.preventDefault()
          self.close()
        })
        this.backfill()
        this._handlerEditor()
      }, 200)
    },
    destroyed () {
      if (editor) {
        editor.toTextArea() // Uninstall
        editor.off($('.code-shell-mirror'), 'keypress', this.keypress)
      }
    },
    // eslint-disable-next-line vue/no-unused-components
    components: { mLocalParams, mListBox, mScriptBox, Treeselect }
  }
</script>
