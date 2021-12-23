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
  <m-popover ref="popover" :ok-text="item && item.name ? $t('Edit') : $t('Submit')" @ok="_ok" @close="close">
    <template slot="content">
      <div class="create-environment-model">
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Task group name')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="name"
                    maxlength="60"
                    size="mini"
                    :placeholder="$t('Please enter name')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Project Name')}}</template>
          <template slot="content">
            <el-select
              v-model="workerGroups"
              size="mini"
              multiple
              collapse-tags
              style="display: block;"
              :placeholder="$t('Please select project')">
              <el-option
                v-for="item in workerGroupOptions"
                :key="item.id"
                :label="item.id"
                :value="item.name">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Task group resource pool size')}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="name"
              maxlength="60"
              size="mini"
              :placeholder="$t('Please enter task group resource pool size')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Task group desc')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="description"
                    maxlength="60"
                    size="mini"
                    :placeholder="$t('Please enter task group desc')">
            </el-input>
          </template>
        </m-list-box-f>
      </div>
    </template>
  </m-popover>
</template>
<script>
  import _ from 'lodash'
  import { mapActions } from 'vuex'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import mPopover from '@/module/components/popup/popover'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'

  export default {
    name: 'create-task-group',
    data () {
      return {
        store,
        name: '',
        projectCode: '',
        groupSize: 0,
        projects: [],
        projectOptions: [],
        workerGroups: [],
        workerGroupOptions: [],
        environment: '',
        config: '',
        description: ''
      }
    },
    props: {
      item: Object
    },
    methods: {
      ...mapActions('projects', ['getProjectsList']),
      _getProjectList () {
        this.getProjectsList().then(res => {
          this.projects = res
          console.log('get project List')
          console.log(this.projects)
        })
      },
      _ok () {
        if (!this._verification()) {
          return
        }

        let param = {
          name: _.trim(this.name),
          config: _.trim(this.config),
          description: _.trim(this.description),
          workerGroups: JSON.stringify(this.workerGroups)
        }

        let $then = (res) => {
          this.$emit('onUpdate')
          this.$message.success(res.msg)
          this.$refs.popover.spinnerLoading = false
        }

        let $catch = (e) => {
          this.$message.error(e.msg || '')
          this.$refs.popover.spinnerLoading = false
        }

        if (this.item && this.item.name) {
          this.$refs.popover.spinnerLoading = true
          let updateParam = {
            code: this.item.code,
            name: _.trim(this.name),
            config: _.trim(this.config),
            description: _.trim(this.description),
            workerGroups: JSON.stringify(this.workerGroups)
          }
          this.store.dispatch('security/updateEnvironment', updateParam).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        } else {
          this._verifyName(param).then(() => {
            this.$refs.popover.spinnerLoading = true
            this.store.dispatch('security/createEnvironment', param).then(res => {
              $then(res)
            }).catch(e => {
              $catch(e)
            })
          }).catch(e => {
            this.$message.error(e.msg || '')
          })
        }
      },
      _verification () {
        if (!this.name.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        if (!this.config.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter environment config')}`)
          return false
        }
        if (!this.description.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter environment desc')}`)
          return false
        }
        return true
      },
      _verifyName (param) {
        return new Promise((resolve, reject) => {
          this.store.dispatch('security/verifyEnvironment', { environmentName: param.name }).then(res => {
            resolve()
          }).catch(e => {
            reject(e)
          })
        })
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {
      item: {
        handler (val, oldVal) {
          this.name = val.name
          this.config = val.config
          this.description = val.description
          this.workerGroups = val.workerGroups
          this.workerGroupOptions = val.workerGroupOptions
        },
        deep: true
      }
    },
    created () {
      if (this.item && this.item.name) {
        this.name = this.item.name
        this.config = this.item.config
        this.description = this.item.description
        this.workerGroups = this.item.workerGroups
      }
      this.workerGroupOptions = this.item.workerGroupOptions
    },
    mounted () {
    },
    components: { mPopover, mListBoxF }
  }
</script>
