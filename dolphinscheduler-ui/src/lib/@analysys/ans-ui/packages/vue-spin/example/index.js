import Vue from 'vue'
import App from './app.vue'
import { xSpin } from '../src'

Vue.use(xSpin)

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
