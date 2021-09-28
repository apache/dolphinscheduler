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
  <m-popover
          ref="popover"
          :ok-text="item ? $t('Edit') : $t('Submit')"
          @ok="_ok"
          @close="close">
    <template slot="content">
      <div class="create-user-model">
        <m-list-box-f>
            <template slot="name"><strong>*</strong>{{$t('TQname')}}</template>
            <template slot="content">
              <el-input
                      type="input"
                      v-model="name"
                      maxlength="60"
                      size="small"
                      :placeholder="$t('Please enter taskqueue name')">
              </el-input>
            </template>
          </m-list-box-f>
        <m-list-box-f >
            <template slot="name"><strong>*</strong>{{$t('TQdes')}}</template>
            <template slot="content">
              <el-input
                      type="input"
                      v-model="description"
                      size="small"
                      :placeholder="$t('Please enter taskqueue description')">
              </el-input>
            </template>
          </m-list-box-f>


          <m-list-box-f>
            <template slot="name"><strong>*</strong>{{$t('TQnum')}}</template>
            <template slot="content">
              <el-input
                      type="input"
                      v-model="groupSize"
                      size="small"
                      :placeholder="$t('Please enter taskqueue size')">
              </el-input>
            </template>
          </m-list-box-f>

      </div>
    </template>
  </m-popover>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import store from '@/conf/home/store'
  import router from '@/conf/home/router'
  import mPopover from '@/module/components/popup/popover'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'
  import { mapActions } from 'vuex'

  export default {
    name: 'create-user',
    data () {
      return {
        store,
        router,
        groupSize:'',
        name:'',
        description:'',
        queueList: [],
        userName: '',
        userPassword: '',
        tenantId: '',
        queueName: '',
        email: '',
        phone: '',
        userState: '1',
        tenantList: [],
        // Source admin user information
        isADMIN: store.state.user.userInfo.userType === 'ADMIN_USER' && router.history.current.name !== 'account'
      }
    },
    props: {
      item: Object,
      fromUserInfo: Boolean
    },
    methods: {
      ...mapActions('security', ['createTaskGroup','updateTaskGroup']),
      _ok () {
        if (this._verification()) {
          this.$refs.popover.spinnerLoading = true
          let params = {
                  name: this.name,
                  description: this.description,
                  groupSize: this.groupSize,

                }
            if (this.item) {
              params.id = this.item.id
              this.updateTaskGroup(params).then(res => {
                    this.$message.success(res.msg)
                    this.$refs.popover.spinnerLoading = false
                    this.$emit('onUpdate', params)
                    }).catch(e => {
                      this.$message.error(e.msg || '')
                      this.$refs.popover.spinnerLoading = false
                    })
            }else{
            this.createTaskGroup(params).then(res => {
              this.$message.success(res.msg)
              this.$refs.popover.spinnerLoading = false
              this.$emit('onUpdate', params)
              }).catch(e => {
                this.$message.error(e.msg || '')
                this.$refs.popover.spinnerLoading = false
              })
            }//else end
        }
      },
      _verification () {
                if (!this.name.replace(/\s*/g, '')) {
                  this.$message.warning(`${i18n.$t('Please enter taskqueue name')}`)
                  return false
                }
                let taskGroupLength = this.name.length
                // TaskGroupQueue name
                if (taskGroupLength < 3 || taskGroupLength > 20) {
                  this.$message.warning(`${i18n.$t('taskgroup name length is between 3 and 20')}`)
                  return false
                }
                if (!this.description.replace(/\s*/g, '')) {
                        this.$message.warning(`${i18n.$t('Please enter taskqueue description')}`)
                        return false
                      }
                if (!this.groupSize.replace(/\s*/g, '')) {
                  this.$message.warning(`${i18n.$t('Please enter taskqueue size')}`)
                  return false
                }
                return true
      },
      close () {
        this.$emit('close')
      }
    },
    watch: {},
    created () {

    },
    mounted () {

    },
    components: { mPopover, mListBoxF }
  }
</script>
