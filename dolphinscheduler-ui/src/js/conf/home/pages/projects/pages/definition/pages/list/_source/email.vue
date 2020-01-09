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
  <div class="ans-input email-model">
    <div class="clearfix input-element" :class="disabled ? 'disabled' : ''">
      <span class="tag-wrapper" v-for="(item,$index) in activeList" :key="$index" :class="activeIndex === $index ? 'active' : ''">
        <span class="tag-text">{{item}}</span>
        <em class="remove-tag ans-icon-close" @click.stop="_del($index)" v-if="!disabled"></em>
      </span>
      <x-poptip
              placement="bottom-start"
              :append-to-body="true"
              :visible-arrow="false"
              v-model="isEmail"
              trigger="manual">
        <div class="email-list-model">
          <div class="ans-scroller" style=" max-height: 300px;">
            <div class="scroll-area-wrapper scroll-transition">
              <ul class="dropdown-container">
                <li class="ans-option" v-for="(item,$index) in emailList" @click.stop="_selectEmail($index + 1)" :key="$index">
                  <span class="default-option-class" :class="index === ($index + 1) ? 'active' : ''">{{item}}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <span class="label-wrapper" slot="reference" >
          <!--@keydown.tab="_emailTab"-->
          <input
                  class="email-input"
                  ref="emailInput"
                  :style="{width:emailWidth + 'px'}"
                  type="text"
                  v-model="email"
                  :disabled="disabled"
                  :placeholder="$t('Please enter email')"
                  @blur="_emailEnter"
                  @keydown.tab="_emailTab"
                  @keyup.delete="_emailDelete"
                  @keyup.enter="_emailEnter"
                  @keyup.space="_emailEnter"
                  @keyup.186="_emailEnter"
                  @keyup.up="_emailKeyup('up')"
                  @keyup.down="_emailKeyup('down')">
        </span>
      </x-poptip>
    </div>
  </div>
</template>
<script>
  import _ from 'lodash'
  import i18n from '@/module/i18n'
  import emailList from '~/external/email'
  import { isEmial, fuzzyQuery } from './util'

  export default {
    name: 'email',
    data () {
      return {
        tagModel: false,
        email: '',
        activeIndex: null,
        emailList: [],
        index: 0,
        emailWidth: 100,
        isCn: false
      }
    },
    props: {
      activeList: Array,
      repeatData: Array,
      disabled: {
        type: Boolean,
        default: false
      }
    },
    model: {
      prop: 'activeList',
      event: 'valueEvent'
    },
    methods: {
      /**
       * Manually add a mailbox
       */
      _manualEmail () {
        if (this.email === '') {
          return true
        }
        this.email = _.trim(this.email).replace(/(;$)|(；$)/g, "")

        let email = this.email

        let is = (n) => {
          return _.some(_.cloneDeep(this.repeatData).concat(_.cloneDeep(this.activeList)), v => v === n)
        }

        if (isEmial(email)) {
          if (!is(email)) {
            this.emailWidth = 0
            this.activeList.push(email)
            this.email = ''
            this._handlerEmailWitch()
            return true
          } else {
            this.$message.warning(`${i18n.$t('Mailbox already exists! Recipients and copyers cannot repeat')}`)
            return false
          }
        } else {
          this.$message.warning(`${i18n.$t('Mailbox input is illegal')}`)
          return false
        }
      },
      /**
       * Processing mailbox
       */
      _handlerEmail (val) {
        if (!val) {
          this.emailList = []
          this.isEmail = false
        } else {
          let a = _.cloneDeep(this.repeatData).concat(_.cloneDeep(this.activeList))
          let b = a.concat(emailList)
          let list = fuzzyQuery(b, val)
          this.emailList = _.uniqWith(list.length && list, _.isEqual)
          this.isEmail = !!list.length
          if (this.emailList.length) {
            this.index = 1
          }
        }
      },
      /**
       * Carriage return
       */
      _emailEnter () {
        // not list Hand filling
        if (!this.emailList.length) {
          this._manualEmail()
          return
        }
        this._selectEmail(this.index)
      },
      /**
       * delete email
       */
      _emailDelete () {
        // Do not delete in case of input method in Chinese
        if (!this.isCn) {
          this.emailWidth = 0
          if (_.isInteger(this.activeIndex)) {
            this.activeList.pop()
            this.activeIndex = null
          } else {
            if (!this.email) {
              this.activeIndex = this.activeList.length - 1
            }
          }
          this._handlerEmailWitch()
        }
      },
      /**
       * click delete
       */
      _del (i) {
        this.emailWidth = 0
        this.activeList.splice(i, 1)
        this._handlerEmailWitch()
      },
      /**
       * keyup Up/down event processing
       */
      _emailKeyup (type) {
        let emailList = this.emailList.length
        if (emailList === 1) {
          this.index = 1
          return
        }
        if (emailList) {
          if (type === 'up') {
            this.index = ((i) => {
              let num
              if (i === 0 || i === 1) {
                num = emailList
              } else {
                num = i - 1
              }
              return num
            })(this.index)
          } else {
            this.index = ((i) => {
              let num
              if (i === 0 || i === emailList) {
                num = 1
              } else {
                num = i + 1
              }
              return num
            })(this.index)
          }
        }
      },
      /**
       * Check mailbox processing
       */
      _selectEmail (i) {
        let item = this.emailList[i - 1]
        this.isEmail = false
        this.email = ''

        // Non-existing data
        if (_.filter(_.cloneDeep(this.repeatData).concat(_.cloneDeep(this.activeList)), v => v === item).length) {
          this.$message.warning(`${i18n.$t('Mailbox already exists! Recipients and copyers cannot repeat')}`)
          return
        }
        // Width initialization
        this.emailWidth = 0
        // Insert data
        this.activeList.push(item)
        // Calculated width
        this._handlerEmailWitch()
        // Check mailbox index initialization
        this.activeIndex = null
        setTimeout(() => {
          // Focus position
          this.$refs.emailInput.focus()
        }, 100)
      },
      /**
       * Processing width
       */
      _handlerEmailWitch () {
        setTimeout(() => {
          this.emailWidth = parseInt($('.email-model').width() - $(this.$refs.emailInput).position().left - 20)
          if (this.emailWidth < 80) {
            this.emailWidth = 200
          }
        }, 100)
      },
      /**
       * Tab event processing
       */
      _emailTab () {
        // Data processing
        this._emailEnter()
      }
    },
    watch: {
      email (val) {
        this._handlerEmail(val)
        // Check mailbox index initialization
        this.activeIndex = null
      },
      activeList (val) {
        this.$emit('valueEvent', val)
      }
    },
    created () {

    },
    mounted () {
      setTimeout(() => {
        // Processing width
        this._handlerEmailWitch()
      }, 500)

      // Input method judgment
      $('.email-input').on('input', function () {
        // Chinese input is not truncated
        if ($(this).prop('comStart')) return
        this.isCn = false
      }).on('compositionstart', () => {
        $(this).prop('comStart', true)
        // Check mailbox index initialization
        this.activeIndex = null
        this.isCn = true
      }).on('compositionend', () => {
        $(this).prop('comStart', false)
        // Check mailbox index initialization
        this.activeIndex = null
        this.isCn = false
      })
    }
  }
</script>

<style lang="scss" rel="stylesheet/scss">
  .email-model {
    width: 100%;
    .input-element {
      min-height: 32px;
      padding: 1px 8px;
      .tag-wrapper {
        display: inline-block;
        height: 22px;
        margin: 3px 8px 3px 0px;
        padding: 0 10px;
        background: #ebf8ff;
        border-radius: 2px;
        cursor: default;
        &.active {
          background: #E2EFF9
        }
        .tag-text {
          margin-right: 8px;
          font-size: 12px;
          line-height: 22px;
          color: #666;
          cursor: text;
        }
        .remove-tag {
          font-size: 12px;
          -webkit-transform: scale(.8);
          -ms-transform: scale(.8);
          transform: scale(.8);
          color: #666;
          cursor: pointer;
          &:hover {
            color: #000;
          }
        }
      }
      .label-wrapper {
        margin-left: -6px;
        input {
          height: 29px;
          line-height: 29px;
          border: 0;
          padding-left: 4px;
        }
      }
      &.disabled {
        .tag-wrapper {
          background: #d9d9d9;
        }
        .email-input {
          background: none;
        }
      }
    }
  }
  .email-list-model {
    margin: -10px -13px;
    .ans-scroller {
      width: 230px;
      overflow-y: scroll;
      .default-option-class {
        width: 230px;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
      }
    }
    .ans-option .default-option-class.active {
      background: #ebf8ff;
    }
  }
</style>
