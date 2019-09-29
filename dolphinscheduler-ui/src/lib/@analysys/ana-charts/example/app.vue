<template>
  <div class="app-container">
    <div class="left">
      <ul class="sidebar">
        <li v-for="(link, i) in links" :key="i">
          <router-link :to="link.url">{{link.name}}</router-link>
          <ul class="sub" v-if="link.subs">
            <li v-for="(sub, j) in link.subs" :key="j">
              <router-link :to="sub.url">{{sub.name}}</router-link>
            </li>
          </ul>
        </li>
      </ul>
    </div>
    <div class="right">
      <router-view />
    </div>
  </div>
</template>

<script>
import './styles/main.scss'

export default {
  name: 'App',
  data () {
    return {
      links: [
        {
          name: '折线图',
          url: '/line',
          subs: [
            { name: '单条折线图', url: '/line#simple' },
            { name: '多条折线图', url: '/line#multiple' },
            { name: '横向折线图', url: '/line#reverse' }
          ]
        },
        {
          name: '柱状图',
          url: '/bar',
          subs: [
            { name: '单条柱状图', url: '/bar#simple' },
            { name: '多条柱状图', url: '/bar#multiple' },
            { name: '横向柱状图', url: '/bar#reverse' },
            { name: '折柱混合图', url: '/bar#mixin' },
            { name: '堆叠柱状图', url: '/bar#stack' },
            { name: '时间轴柱状图', url: '/bar#timeline' }
          ]
        },
        {
          name: '饼状图',
          url: '/pie',
          subs: [
            { name: '简单饼状图', url: '/pie#simple' },
            { name: '环形图', url: '/pie#ring' }
          ]
        },
        {
          name: '雷达图',
          url: '/radar',
          subs: [
            { name: '简单雷达图', url: '/radar#simple' }
          ]
        },
        {
          name: '漏斗图',
          url: '/funnel',
          subs: [
            { name: '简单漏斗图', url: '/funnel#simple' }
          ]
        },
        {
          name: '气泡图',
          url: '/scatter',
          subs: [
            { name: '气泡图', url: '/scatter#simple' }
          ]
        }
      ]
    }
  }
}
</script>

<style lang="scss">

</style>
