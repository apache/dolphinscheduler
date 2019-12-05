<template>
  <div class="select-demo-wrapper">
    <section class="demo-section">
      <h4>异步 Option</h4>
      <div>
        <x-select
          ref="select"
          :input-props="{
            'no-border': true
          }"
          :max-height="100"
          multiple
          filterable
          disabled
          :filter-props="['value', 'label']"
          :width="200"
          v-model="defaultValue"
          value-key="id"
          drop-animation="x-ani-fade"
        >
          <div slot="header" slot-scope="scope">这里是自定义头部</div>
          <x-option
            v-for="city in list"
            :key="city.value"
            :value="city"
            :label="city.label"
          >
          </x-option>
          <div slot="footer" slot-scope="scope">这里是自定义底部</div>
        </x-select>
      </div>
    </section>
  </div>
</template>

<script>
import { xSelect, xOption, xOptionGroup } from '../src'

export default {
  name: 'app',
  components: { xSelect, xOption, xOptionGroup },
  data () {
    return {
      list: [],
      cities: [{
        id: 1,
        value: 'Beijing',
        label: '北京'
      }, {
        id: 2,
        value: 'Shanghai',
        label: '上海'
      }, {
        id: 3,
        value: 'Nanjing',
        label: '南京'
      }, {
        id: 4,
        value: 'Chengdu',
        label: '成都'
      }, {
        id: 5,
        value: 'Shenzhen',
        label: '深圳'
      }, {
        id: 6,
        value: 'Guangzhou',
        label: '广州'
      }],
      defaultValue: [2, 3],
      disabled: false
    }
  },
  mounted () {
    setTimeout(() => {
      console.log('loaded')
      this.list = this.cities
      this.disabled = true
    }, 2000)
  }
}
</script>

<style lang="scss">
.demo-wrapper {
  .cate {
    height: 40px;
    margin-left: 20px;
    font-size: 16px;
    font-weight: bold;
    line-height: 40px;
  }
  .row {
    display: flex;
    margin: 20px 0;
    padding: 0 50px;
    & > div {
      margin-right: 50px;
    }
  }
  .custom {
    display: flex;
    justify-content: space-between;
    padding: 5px 10px;
    &:hover {
      background: #8492a6;
      .left,
      .right {
        color: #fff;
      }
    }
    .left {
      font-size: 12px;
    }
    .right {
      color: #8492a6;
      font-size: 12px;
      transform: scale(0.8);
    }
  }
  .selected .custom {
    color: #598cd4;
    background: #dfd8e4;
  }
  .custom-group {
    height: 40px;
    line-height: 40px;
    font-size: 14px;
    color: #303030;
    font-weight: bolder;
    background: #b7d1f0;
    i {
      margin: 0 10px;
    }
  }
}
</style>
