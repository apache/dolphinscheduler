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
      <div class="create-task-group-model">
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
              :disabled="item.modalType==='edit'"
              v-model="projectCode"
              size="mini"
              collapse-tags
              style="display: block;"
              :placeholder="$t('Please select project')">
              <el-option
                v-for="item in projectOptions"
                :key="item.code"
                :label="item.name"
                :value="item.code">
              </el-option>
            </el-select>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Task group resource pool size')}}</template>
          <template slot="content">
            <el-input
              type="input"
              v-model="groupSize"
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
        groupSize: 10,
        projects: [],
        project: [],
        projectOptions: [],
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
        })
      },
      _ok () {
        if (!this._verification()) {
          return
        }

        let param = {
          name: _.trim(this.name),
          projectCode: this.projectCode,
          groupSize: _.trim(this.groupSize),
          description: _.trim(this.description)
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
            id: this.item.id,
            name: _.trim(this.name),
            groupSize: _.trim(this.groupSize),
            description: _.trim(this.description)
          }
          this.store.dispatch('resource/updateTaskGroup', updateParam).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        } else {
          this.$refs.popover.spinnerLoading = true
          this.store.dispatch('resource/createTaskGroup', param).then(res => {
            $then(res)
          }).catch(e => {
            $catch(e)
          })
        }
      },
      _verification () {
        if (!this.name || !this.name.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter name')}`)
          return false
        }
        if (this.groupSize < 1) {
          this.$message.warning(`${i18n.$t('Task group resource pool size be a number')}`)
          return false
        }
        if (!this.description || !this.description.replace(/\s*/g, '')) {
          this.$message.warning(`${i18n.$t('Please enter task group desc')}`)
          return false
        }
        return true
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {
      item: {
        handler (val, oldVal) {
          this.name = val.name
          this.projectCode = val.projectCode
          this.groupSize = val.groupSize
          this.description = val.description
          this.projectOptions = val.projectOptions
          this.modalType = val.modalType
        },
        deep: true
      }
    },
    created () {
      if (this.item && this.item.name) {
        this.name = this.item.name
        this.projectCode = this.item.projectCode
        this.groupSize = this.item.groupSize
        this.description = this.item.description
      }
      this.projectOptions = this.item.projectOptions
    },
    mounted () {
    },
    components: { mPopover, mListBoxF }
  }
</script>
