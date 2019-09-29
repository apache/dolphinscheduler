<template>
  <div>
    <section class="demo-section">
      <h4>基本用法</h4>
      <div>
        <x-page :current="current" :total="total" @on-change="pageChanged"></x-page>
      </div>
    </section>
    <section class="demo-section">
      <h4>显示总条数</h4>
      <div>
        <x-page :current="current" :total="total" show-total @on-change="pageChanged"></x-page>
      </div>
    </section>
    <section class="demo-section">
      <h4>跳转</h4>
      <div>
        <x-page :current="current" :total="total" show-total show-elevator @on-change="pageChanged"></x-page>
      </div>
    </section>
    <section class="demo-section">
      <h4>每页条数</h4>
      <div>
        <x-page :current="current" :total="total" show-total show-elevator show-sizer @on-change="pageChanged"></x-page>
      </div>
    </section>
    <section class="demo-section">
      <h4>无边框</h4>
      <div>
        <x-page :current="current" :total="total" small @on-change="pageChanged"></x-page>
      </div>
    </section>
    <section class="demo-section">
      <h4>迷你版</h4>
      <div>
        <x-page :current="current" :total="total" simple @on-change="pageChanged"></x-page>
      </div>
    </section>    
  </div>
</template>

<script>
import { xPage } from '../src'

export default {
  data: function () {
    return {
      current: 3,
      total: 100
    }
  },
  methods: {
    pageChanged (v) {
      console.log(v)
    }
  },
  components: { xPage },
  mounted () {

  }
}
</script>

<style lang="scss"></style>
