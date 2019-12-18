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
  <div class="nav-model">
    <router-link :to="{ path: '/home'}" tag="div" class="logo-box">
      <a href="javascript:"></a>
    </router-link>
    <div class="nav-box">
      <div class="clearfix list">
        <div class="nav-links">
          <router-link :to="{ path: '/home'}" tag="a" active-class="active">
            <span><i class="iconfont">&#xe61d;</i>{{$t('Home')}}</span><b></b>
          </router-link>
        </div>
      </div>
      <div class="clearfix list">
        <div class="nav-links">
          <router-link :to="{ path: '/projects'}" tag="a" active-class="active">
            <span><i class="iconfont">&#xe635;</i>{{$t('Project Manage')}}</span><b></b>
          </router-link>
        </div>
      </div>
      <div class="clearfix list">
        <div class="nav-links">
          <router-link :to="{ path: '/resource'}" tag="a" active-class="active">
            <span><i class="iconfont">&#xe768;</i>{{$t('Resources manage')}}</span><b></b>
          </router-link>
        </div>
      </div>
      <div class="clearfix list">
        <div class="nav-links">
          <router-link :to="{ path: '/datasource'}" tag="a" active-class="active">
            <span><i class="iconfont">&#xeef4;</i>{{$t('Datasource manage')}}</span><b></b>
          </router-link>
        </div>
      </div>
      <div class="clearfix list">
        <div class="nav-links">
          <router-link :to="{ path: '/monitor'}" tag="a" active-class="active">
            <span><i class="iconfont">&#xe65f;</i>{{$t('Monitor')}}</span><b></b>
          </router-link>
        </div>
      </div>
      <div class="clearfix list" >
        <div class="nav-links">
          <router-link :to="{ path: '/security'}" tag="a" active-class="active" v-ps="['ADMIN_USER']">
            <span><i class="iconfont">&#xe671;</i>{{$t('Security')}}</span><b></b>
          </router-link>
        </div>
      </div>
    </div>
    <div class="right">
      <!--<span class="docs">
        <a :href="docLink">doc</a>
      </span>-->
      <span class="lang">
        <x-poptip
                style="width: 80px"
                trigger="click">
        <div class="lrns-list">
          <a href="javascript:" @click="_toggleLanguage(item.code)" v-for="(item,$index) in localeList" :key="$index"><span>{{item.name}}</span></a>
        </div>
        <div class="login-model" slot="reference">
          <span>{{activeLocale.name}}</span>
          <i class="ans-icon-arrow-down"></i>
        </div>
      </x-poptip>
      </span>
      <x-poptip
              ref="login"
              trigger="click"
              v-model="isLogin"
              placement="bottom-end">
        <div class="lrns-list">
          <a href="javascript:" @click="_goAccount">
            <i class="ans-icon-user-empty"></i>
            <span>{{$t('User Information')}}</span>
          </a>
          <a href="javascript:" @click="_signOut">
            <i class="ans-icon-off"></i>
            <span>{{$t('Logout')}}</span>
          </a>
        </div>
        <div class="login-model" slot="reference">
          <i class="ans-icon-user-solid"></i>
          <span>{{userInfo.userName}}</span>
          <i class="ans-icon-arrow-down"></i>
        </div>
      </x-poptip>
    </div>
    <div class="file-update-model" @click="_toggleArchive" v-if="isUpdate">
      <div class="icon-cloud">
        <i class="fa fa-cloud-upload"></i>
      </div>
      <div class="progress-box">
        <m-progress-bar :value="progress" text-placement="bottom"></m-progress-bar>
      </div>
    </div>
    <div class="adaptive-m-nav">
      <div class="m-nav-box ">
        <a href="javascript:" @click="mIsNav = !mIsNav"><i class="iconfont">&#xe609;</i></a>
      </div>
      <div class="m-title-box">
        <div class="logo-m"></div>
      </div>
      <div class="m-user-box">
        <a href="javascript:" @click="_goAccount"><i class="iconfont">&#xe64a;</i></a>
      </div>
      <transition name="slide-fade">
        <div class="m-nav-list" v-if="mIsNav">
          <ul @click="mIsNav = false">
            <router-link :to="{ path: '/home'}" tag="li" active-class="active">
              <i class="iconfont">&#xe61d;</i>
              <span>{{$t('Home')}}</span>
            </router-link>
            <router-link :to="{ path: '/projects'}" tag="li" active-class="active">
              <i class="iconfont">&#xe635;</i>
              <span>{{$t('Project manage')}}</span>
            </router-link>
            <router-link :to="{ path: '/resource'}" tag="li" active-class="active">
              <i class="iconfont">&#xe768;</i>
              <span>{{$t('Resources manage')}}</span>
            </router-link>
            <router-link :to="{ path: '/datasource'}" tag="li" active-class="active">
              <i class="iconfont">&#xeef4;</i>
              <span>{{$t('Datasource manage')}}</span>
            </router-link>
            <router-link :to="{ path: '/security'}" tag="li" active-class="active" v-ps="['ADMIN_USER']">
              <i class="iconfont">&#xe671;</i>
              <span>{{$t('Security')}}</span>
            </router-link>
          </ul>
        </div>
      </transition>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import cookie from '@/module/util/cookie'
  import { mapState, mapActions } from 'vuex'
  import { findComponentDownward } from '@/module/util/'
  import mFileUpdate from '@/module/components/fileUpdate/fileUpdate'
  import mDefinitionUpdate from '@/module/components/fileUpdate/definitionUpdate'
  import mProgressBar from '@/module/components/progressBar/progressBar'

  import { findLocale, localeList } from '@/module/i18n/config'

  export default {
    name: 'roof-nav',
    data () {
      return {
        // Whether to drag
        isDrag: false,
        // Upload progress
        progress: 0,
        // Whether to upload
        isUpdate: false,
        // Whether to log in
        isLogin: false,
        // Mobile compatible navigation
        mIsNav: false,
        // Take the language list data to get rid of the language pack
        localeList: _.map(_.cloneDeep(localeList()), v => _.omit(v, ['locale'])),
        // Selected language
        activeLocale: '',
        // Environmental variable
        docLink: ''
      }
    },

    methods: {
      ...mapActions('user', ['signOut']),
      /**
       * User Info
       */
      _goAccount () {
        this.isLogin = false
        this.$router.push({ name: 'account' })
      },
      /**
       * Upload (for the time being)
       */
      _fileUpdate (type) {
        if (this.progress) {
          this._toggleArchive()
          return
        }
        let self = this
        let modal = this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'update-file-modal',
          transitionName: 'opacityp',
          render (h) {
            if(type === 'DEFINITION'){
              return h(mDefinitionUpdate, {
                on: {
                  onProgress (val) {
                    self.progress = val
                  },
                  onUpdate () {
                    findComponentDownward(self.$root, `definition-list-index`)._updateList()
                    self.isUpdate = false
                    self.progress = 0
                    modal.remove()
                  },
                  onArchive () {
                    self.isUpdate = true
                  },
                  close () {
                    self.progress = 0
                    modal.remove()
                  }
                },
                props: {
                  type: type
                }
              })
            }else{
              return h(mFileUpdate, {
                on: {
                  onProgress (val) {
                    self.progress = val
                  },
                  onUpdate () {
                    findComponentDownward(self.$root, `resource-list-index-${type}`)._updateList()
                    self.isUpdate = false
                    self.progress = 0
                    modal.remove()
                  },
                  onArchive () {
                    self.isUpdate = true
                  },
                  close () {
                    self.progress = 0
                    modal.remove()
                  }
                },
                props: {
                  type: type
                }
              })
            }
          }
        })
      },
      /**
       * Upload popup layer display
       */
      _toggleArchive () {
        $('.update-file-modal').show()
      },
      /**
       * sign out
       */
      _signOut () {
        this.signOut()
      },
      /**
       * Language switching
       */
      _toggleLanguage (language) {
        cookie.set('language', language, { path: '/' })
        setTimeout(() => {
          window.location.reload()
        }, 100)
      }
    },
    created () {
      let language = cookie.get('language')
      this.activeLocale = language ? findLocale(language) : '中文'
      this.docLink = process.env.NODE_ENV === 'true' ? 'docs' : `/view/docs/${this.activeLocale.code}/_book` // eslint-disable-line
    },
    computed: {
      ...mapState('user', ['userInfo'])
    },
    components: { mFileUpdate, mProgressBar, mDefinitionUpdate }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .nav-model {
    height: 60px;
    background: #2D303A;
    box-shadow: 0 3px 5px rgba(0, 0, 0, 0.3);
    position: relative;
    .m-title-box {
      text-align: center;
      .logo-m {
        width: 36px;
        height: 36px;
        background: url("./m_logo.png");
        margin: 0 auto;
        position: relative;
        top: 12px;
      }
    }
    .logo-box {
      position: absolute;
      left: 10px;
      top: 8px;
      cursor: pointer;
      >a {
        width: 180px;
        height: 46px;
        display: block;
        background: url("./logo.svg") no-repeat;
      }
    }
    .nav-box {
      height: 60px;
      line-height: 60px;
      position: absolute;
      left: 220px;
      top: 0;
      .list {
        width: 106px;
        float: left;
        position: relative;
        cursor: pointer;
        margin-right: 14px;
        .nav-links {
          height: 60px;
          a {
            position: relative;
            text-decoration: none;
            font-size: 15px;
            color: #333;
            display: inline-block;
            float: left;
            text-align: center;
            span {
              display: block;
              width: 106px;
              color: #fff;
              .iconfont {
                vertical-align: -2px;
                font-size: 22px;
                margin-right: 4px;
              }
            }
            &:hover {
              span {
                color: #2d8cf0;
              }
            }
            &.active {
              span {
                color: #2D8BF0;
                i {
                  color: #2d8cf0;
                }
              }
              b {
                height: 2px;
                background: #2d8cf0;
                display: block;
                margin-top: -2px;
              }
            }
          }
        }
      }
    }
    .right {
      position: absolute;
      right: 0;
      top: 0;
      .ans-poptip {
        min-width: 120px;
      }
      >.lang {
        .ans-poptip {
          min-width: 80px;
        }
      }
      >.docs {
        padding-right: 20px;
        a {
          color: #fff;
          font-size: 14px;
          vertical-align: middle;
          &:hover {
            color: #2d8bf0;
          }
        }
      }
      .current-project {
        height: 60px;
        line-height: 56px;
        display: inline-block;
        margin-right: 16px;
        cursor: pointer;
        i {
          font-size: 18px;
          vertical-align: middle;
          &:nth-child(1) {
            font-size: 20px;
            color:#2d8cf0;
          }
        }
        span {
          color: #333;
          vertical-align: middle;
          font-size: 14px;
        }
        &:hover {
          span {
            color: #2d8cf0;
          }
        }
      }
      .login-model {
        display: inline-block;
        margin-right: 20px;
        cursor: pointer;
        margin-top: 16px;
        i {
          font-size: 18px;
          vertical-align: middle;
          color: #fff;
        }
        span {
          color: #fff;
          vertical-align: middle;
          font-size: 14px;
        }
        &:hover {
          >i,>span {
            color: #2d8cf0;
          }
        }

      }
      .lrns-list {
        margin: -6px 0;
        a {
          display: block;
          height: 30px;
          line-height: 30px;
          font-size: 14px;
          &:hover {
            i,span {
              color: #2d8cf0;
            }
          }
        }
      }
    }
    .file-update-model {
      position: absolute;
      right: 160px;
      top: 18px;
      cursor: pointer;
      .progress-box {
        width: 240px;
      }
      .icon-cloud {
        position: absolute;
        left: -34px;
        top: 2px;
        > i {
          font-size: 22px;
          color: #2d8cf0;
        }
      }
    }
  }
</style>
