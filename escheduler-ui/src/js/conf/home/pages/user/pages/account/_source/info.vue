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
  <div class="user-info-model">
    <m-list-box-f>
      <template slot="name">{{$t('User Name')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Email')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.email}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Phone')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.phone}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Permission')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userType === 'GENERAL_USER' ? `${$t('Ordinary users')}` : `${$t('Administrator')}`}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f v-ps="['GENERAL_USER']">
      <template slot="name">{{$t('Tenant')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.tenantName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f v-ps="['GENERAL_USER']">
      <template slot="name">{{$t('Queue')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.queueName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Create Time')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.createTime | formatDate}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('Update Time')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.updateTime | formatDate}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">&nbsp;</template>
      <template slot="content">
        <x-button type="primary" shape="circle" @click="_edit()" >{{$t('Edit')}}</x-button>
      </template>
    </m-list-box-f>
  </div>
</template>
<script>
  import { mapState, mapMutations } from 'vuex'
  import mListBoxF from '@/module/components/listBoxF/listBoxF'
  import mCreateUser from '@/conf/home/pages/security/pages/users/_source/createUser'

  export default {
    name: 'user-info',
    data () {
      return {}
    },
    props: {},
    methods: {
      ...mapMutations('user', ['setUserInfo']),
      /**
       * edit
       */
      _edit () {
        let item = this.userInfo
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          render (h) {
            return h(mCreateUser, {
              on: {
                onUpdate (param) {
                  self.setUserInfo({
                    userName: param.userName,
                    userPassword: param.userPassword,
                    email: param.email,
                    phone: param.phone
                  })
                  modal.remove()
                },
                close () {
                }
              },
              props: {
                item: item
              }
            })
          }
        })
      }
    },
    watch: {},
    created () {
    },
    mounted () {
    },
    computed: {
      ...mapState('user', ['userInfo'])
    },
    components: { mListBoxF }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .user-info-model {
    padding-top: 30px;
    .list-box-f {
      .text {
        width: 200px;
        font-size: 14px;
        color: #888;
      }
      .cont {
        width: calc(100% - 210px);
        margin-left: 10px;
        .sp1 {
          font-size: 14px;
          color: #333;
          display: inline-block;
          padding-top: 6px;
        }
      }
    }
  }
</style>
