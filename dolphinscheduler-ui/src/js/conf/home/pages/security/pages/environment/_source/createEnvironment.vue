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
          <template slot="name"><strong>*</strong>{{$t('Environment Name')}}</template>
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
        <template slot="name"><strong>*</strong>{{$t('Environment Config')}}</template>
        <template slot="content">
          <el-input
                  type="textarea"
                  :autosize="{ minRows: 10, maxRows: 20 }"
                  v-model="config"
                  :placeholder="configExample">
          </el-input>
        </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name"><strong>*</strong>{{$t('Environment Desc')}}</template>
          <template slot="content">
            <el-input
                    type="input"
                    v-model="description"
                    maxlength="60"
                    size="mini"
                    :placeholder="$t('Please enter environment desc')">
            </el-input>
          </template>
        </m-list-box-f>
        <m-list-box-f>
          <template slot="name">{{$t('Environment Worker Group')}}</template>
          <template slot="content">
            <el-select
              v-model="workerGroups"
              size="mini"
              multiple
              collapse-tags
              style="display: block;"
              :placeholder="$t('Please select worker groups')">
              <el-option
                v-for="item in workerGroupOptions"
                :key="item.id"
                :label="item.id"
                :value="item.name">
              </el-option>
            </el-select>
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
    name: 'create-environment',
    data () {
      return {
        store,
        workerGroups: [],
        workerGroupOptions: [],
        environment: '',
        name: '',
        config: '',
        description: '',
        configExample: 'export HADOOP_HOME=/opt/hadoop-2.6.5\n' +
          'export HADOOP_CONF_DIR=/etc/hadoop/conf\n' +
          'export SPARK_HOME=/opt/soft/spark\n' +
          'export PYTHON_HOME=/opt/soft/python\n' +
          'export JAVA_HOME=/opt/java/jdk1.8.0_181-amd64\n' +
          'export HIVE_HOME=/opt/soft/hive\n' +
          'export FLINK_HOME=/opt/soft/flink\n' +
          'export DATAX_HOME=/opt/soft/datax\n' +
          'export YARN_CONF_DIR=/etc/hadoop/conf\n' +
          'export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH\n' +
          'export HADOOP_CLASSPATH=`hadoop classpath`\n'
      }
    },
    props: {
      item: Object
    },
    methods: {
      ...mapActions('security', ['getWorkerGroupsAll']),
      _getWorkerGroupList () {
        this.getWorkerGroupsAll().then(res => {
          this.workerGroups = res
          console.log('get Worker Group List')
          console.log(this.workerGroups)
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
