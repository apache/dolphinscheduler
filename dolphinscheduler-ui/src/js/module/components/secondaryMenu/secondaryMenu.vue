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
  <div class="secondary-menu-model" :class="className" >
    <div class="toogle-box">
      <a href="javascript:" class="tog-close" @click="_toggleMenu" v-if="!isTogHide"></a>
      <a href="javascript:" class="tog-open" @click="_toggleMenu" v-if="isTogHide"></a>
    </div>
    <div class="leven-1" v-for="(item,$index) in menuList" :key="$index">
      <div v-if="item.disabled">
        <template v-if="item.path">
          <router-link :to="{ name: item.path}">
            <div class="name" @click="_toggleSubMenu(item)">
              <a href="javascript:">
                <em class="fa icon" :class="item.icon"></em>
                <span>{{item.name}}</span>
                <em class="fa angle" :class="item.isOpen ? 'ans-icon-arrow-down' : 'ans-icon-arrow-right'" v-if="item.children.length"></em>
              </a>
            </div>
          </router-link>
        </template>
        <template v-if="!item.path">
          <div class="name" @click="_toggleSubMenu(item)">
            <a href="javascript:">
              <em class="fa icon" :class="item.icon"></em>
              <span>{{item.name}}</span>
              <em class="fa angle" :class="item.isOpen ? 'ans-icon-arrow-down' : 'ans-icon-arrow-right'" v-if="item.children.length"></em>
            </a>
          </div>
        </template>
        <ul v-if="item.isOpen && item.children.length">
          <template v-for="(el,index) in item.children">
            <router-link :to="{ name: el.path}" tag="li" active-class="active" v-if="el.disabled" :key="index">
              <span>{{el.name}}</span>
            </router-link>
          </template>
        </ul>
      </div>
    </div>
  </div>
</template>
<script>
  import menu from './_source/menu'
  import store from '@/conf/home/store'
  export default {
    name: 'secondary-menu',
    data () {
      return {
        menuList: menu(this.type),
        index: 0,
        id: this.$route.params.id,
        isTogHide: false,
        isLeft: true
      }
    },
    props: {
      type: String,
      className: String
    },
    watch: {
      isTogHide (is) {
        let layoutBox = $('.main-layout-box')
        is ? layoutBox.addClass('toggle') : layoutBox.removeClass('toggle')
      }
    },
    methods: {
      _toggleSubMenu (item) {
        item.isOpen = !item.isOpen
      },
      _toggleMenu () {
        this.isTogHide = !this.isTogHide
        if(this.isTogHide) {
          sessionStorage.setItem('isLeft',0)
          store.commit('projects/setSideBar',0)
        } else {
          sessionStorage.setItem('isLeft',1)
          store.commit('projects/setSideBar',1)
        }
      }
    },
    mounted () {
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .main-layout-box {
    &.toggle {
      padding-left: 0;
      >.secondary-menu-model {
        left:-200px;
      }
    }
  }
  .secondary-menu-model {
    position: fixed;
    left: 0;
    top: 0;
    width: 200px;
    background: #41444C;
    height: calc(100%);
    padding-top: 80px;
    .toogle-box {
      position: absolute;
      right: -1px;
      top: calc(50% - 50px);
      .tog-close {
        width: 12px;
        height: 102px;
        background: url("./_source/close.png") no-repeat;
        display: inline-block;
      }
      .tog-open {
        width: 12px;
        height: 102px;
        background: url("./_source/open.png") no-repeat;
        display: inline-block;
        position: absolute;
        right: -12px;
        top: 0;
      }
    }
    .leven-1 {
      .name {
        a {
          height: 40px;
          line-height: 40px;
          display: block;
          position: relative;
          padding-left: 10px;
          >.icon {
            vertical-align: middle;
            font-size: 15px;
            width: 20px;
            text-align: center;
            color: #fff;
          }
          >span {
            vertical-align: middle;
            padding-left: 2px;
            font-size: 14px;
            color: #fff;
          }
          >.angle {
            position: absolute;
            right: 12px;
            top: 3px;
          }

        }
      }
      ul {
        li {
          height: 36px;
          line-height: 36px;
          cursor: pointer;
          padding-left: 39px;
          color: #fff;
          a {
            font-size: 14px;
          }
          &.active {
            border-right: 2px solid #2d8cf0;
            background: #2C2F39;
            span {
              font-weight: bold;
              color: #2d8cf0;
            }
          }
        }
      }
      >.router-link-exact-active,.router-link-active {
        background: #f0f6fb;
        .name {
          border-right: 2px solid #2d8cf0;
          background: #2B2E38;
          a {
            span {
              color: #2d8cf0;
              font-weight: bold;
            }
            .fa {
              color: #2d8cf0;
            }
          }
        }
      }
    }
  }
</style>
