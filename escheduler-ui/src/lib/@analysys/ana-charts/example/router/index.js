import Vue from 'vue'
import Router from 'vue-router'
import Line from '../packages/line'
import Bar from '../packages/bar'
import Pie from '../packages/pie'
import Radar from '../packages/radar'
import Funnel from '../packages/funnel'
import Scatter from '../packages/scatter'

Vue.use(Router)

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/line',
      component: Line
    },
    {
      path: '/bar',
      component: Bar
    },
    {
      path: '/pie',
      component: Pie
    },
    {
      path: '/radar',
      component: Radar
    },
    {
      path: '/funnel',
      component: Funnel
    },
    {
      path: '/scatter',
      component: Scatter
    }
  ]
})

router.afterEach((to, from) => {
  if (to.hash) {
    const target = document.querySelector(to.hash)
    if (target) {
      window.scrollTo(0, target.offsetTop)
    }
  } else {
    window.scrollTo(0, 0)
  }
})

export default router
