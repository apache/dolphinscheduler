import Vue from 'vue'
import App from './app.vue'
import '@analysys/ans-ui/lib/ans-ui.min.css'
import ans from '@analysys/ans-ui/lib/ans-ui.min.js'

Vue.use(ans)

new Vue({
  el: '#app',
  render: h => h(App),
  mounted () {
    console.log('success')
  }
})
