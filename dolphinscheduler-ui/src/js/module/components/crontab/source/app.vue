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
  <div class="v-crontab">

    <!--<div style="border: 1px solid red;padding: 10px">-->
      <!--&lt;!&ndash;<span style="display: block">{{secondVal}} {{minuteVal}} {{hourVal}} {{dayVal}} {{monthVal}} {{weekVal}} {{yearVal}}</span>&ndash;&gt;-->
      <!--{{rtValue}}-->
    <!--</div>-->

    <div class="v-crontab-model">
      <div class="clearfix v-crontab-tab">
        <a href="javascript:" :class="tabVal === 'second' ?' active' :''" @click="onTab('second')"><em class="ans-icon-calendar"></em><span>{{$t('秒')}}</span></a>
        <a href="javascript:" :class="tabVal === 'minute' ?' active' :''" @click="onTab('minute')"><em class="ans-icon-calendar"></em><span>{{$t('分')}}</span></a>
        <a href="javascript:" :class="tabVal === 'hour' ?' active' :''" @click="onTab('hour')"><em class="ans-icon-calendar"></em><span>{{$t('时')}}</span></a>
        <a href="javascript:" :class="tabVal === 'day' ?' active' :''" @click="onTab('day')"><em class="ans-icon-calendar"></em><span>{{$t('天')}}</span></a>
        <a href="javascript:" :class="tabVal === 'month' ?' active' :''" @click="onTab('month')"><em class="ans-icon-calendar"></em><span>{{$t('月')}}</span></a>
        <a href="javascript:" :class="tabVal === 'year' ?' active' :''" @click="onTab('year')"><em class="ans-icon-calendar"></em><span>{{$t('年')}}</span></a>
      </div>
      <div class="v-crontab-content">
        <template v-if="tabVal === 'second'">
          <m-second v-model="secondVal"></m-second>
        </template>
        <template v-if="tabVal === 'minute'">
          <m-minute v-model="minuteVal"></m-minute>
        </template>
        <template v-if="tabVal === 'hour'">
          <m-hour v-model="hourVal"></m-hour>
        </template>

        <!-- day start -->
        <template v-if="tabVal === 'day'">
          <m-day @on-day-value="_onDayValue" @on-week-value="_onWeekValue" :day-val="dayVal" :week-val="weekVal"></m-day>
        </template>
        <!-- day end -->

        <template v-if="tabVal === 'month'">
          <m-month v-model="monthVal"></m-month>
        </template>

        <template v-if="tabVal === 'year'">
          <m-year v-model="yearVal"></m-year>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
  import i18n from './_source/i18n'
  import mSecond from './_times/second'
  import mMinute from './_times/minute'
  import mHour from './_times/hour'
  import mDay from './_times/day'
  import mMonth from './_times/month'
  import mYear from './_times/year'

  export default {
    name: 'app',
    data () {
      return {
        tabVal: 'second',
        secondVal: '*',
        minuteVal: '*',
        hourVal: '*',
        dayVal: '*',
        monthVal: '*',
        weekVal: '?',
        yearVal: '*',
        watchValue: ''
      }
    },
    mixins: [i18n],
    props: {
      value: {
        type: String,
        default: '* * * * * ? *'
      },
      locale: {
        type: String,
        default: 'en_US'
      }
    },
    model: {
      prop: 'value',
      event: 'valueEvent'
    },
    methods: {
      onTab (val) {
        this.tabVal = val
      },
      _onDayValue (val) {
        this.dayVal = val
      },
      _onWeekValue (val) {
        this.weekVal = val
      },
      _reset () {
        let str = this.value.split(' ')
        this.secondVal = str[0]
        this.minuteVal = str[1]
        this.hourVal = str[2]
        this.dayVal = str[3]
        this.monthVal = str[4]
        this.weekVal = str[5]
        this.yearVal = str[6]
      }
    },
    watch: {
      rtValue (val) {
        this.$emit('valueEvent', val)
      },
      value () {
        this._reset()
      }
    },
    beforeCreate () {
    },
    created () {
      // International binding under win
      window.localeCrontab = this.locale

      // Initialization
      this._reset()
    },
    beforeMount () {
    },
    mounted () {
    },
    beforeUpdate () {
    },
    updated () {
    },
    beforeDestroy () {
    },
    destroyed () {
    },
    computed: {
      rtValue () {
        return `${this.secondVal} ${this.minuteVal} ${this.hourVal} ${this.dayVal} ${this.monthVal} ${this.weekVal} ${this.yearVal}`
      }
    },
    components: { mSecond, mMinute, mHour, mDay, mMonth, mYear }
  }
</script>

<style lang="scss">
  @import "index";
  .v-crontab {
    width: 640px;
    border:1px solid #e4e7ed;
    background: #fff;
    border-radius: 4px;
    .v-crontab-model {
      .v-crontab-tab {
        background: #f5f7fa;
        height: 42px;
        border-bottom: 1px solid #e4e7ed;
        a {
          width: 86px;
          height: 42px;
          line-height: 42px;
          text-align: center;
          float: left;
          border-right: 1px solid #e4e7ed;
          text-decoration: none;
          span,i {
            font-size: 14px;
            color: #888;
            vertical-align: middle;
          }
          i {
            margin-right: 5px;
          }
          &:hover{
            text-decoration: none;
          }
          &.active {
            background: #fff;
            position: relative;
            height: 43px;
          }
        }
      }
      .v-crontab-content {
        padding: 8px 20px;
        padding-bottom: 12px;
      }
    }
  }

</style>
