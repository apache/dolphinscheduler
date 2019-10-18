/**
 * Created by tangwei on 17/8/25.
 */
import Vue from 'vue'
import BoxManager from './BoxManager.vue'

BoxManager.newInstance = properties => {
  const _props = properties || {}

  const Instance = new Vue({
    data: _props,
    render (h) {
      return h(BoxManager, {
        props: _props
      })
    }
  })

  const component = Instance.$mount()
  document.body.appendChild(component.$el)
  const notification = Instance.$children[0]

  return {
    notice (noticeProps) {
      notification.add(noticeProps)
    },
    remove (name) {
      notification.close(name)
    },
    component: notification,
    destroy (classname) {
      notification.closeAll()
      setTimeout(function () {
        document.body.removeChild(document.getElementsByClassName(classname)[0])
      }, 500)
    }
  }
}

export default BoxManager
