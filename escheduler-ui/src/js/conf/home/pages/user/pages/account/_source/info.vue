<template>
  <div class="user-info-model">
    <m-list-box-f>
      <template slot="name">{{$t('用户名称')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('邮箱')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.email}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('手机')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.phone}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('权限')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.userType === 'GENERAL_USER' ? `${$t('普通用户')}` : `${$t('管理员')}`}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f v-ps="['GENERAL_USER']">
      <template slot="name">{{$t('租户')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.tenantName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f v-ps="['GENERAL_USER']">
      <template slot="name">{{$t('队列')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.queueName}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('创建时间')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.createTime | formatDate}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">{{$t('更新时间')}}</template>
      <template slot="content">
        <span class="sp1">{{userInfo.updateTime | formatDate}}</span>
      </template>
    </m-list-box-f>
    <m-list-box-f>
      <template slot="name">&nbsp;</template>
      <template slot="content">
        <x-button type="primary" shape="circle" @click="_edit()" >{{$t('修改')}}</x-button>
      </template>
    </m-list-box-f>
  </div>
</template>
<script>
  import { mapState, mapMutations } from 'vuex'
  import '@/module/filter/formatDate'
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
