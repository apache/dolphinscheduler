'use strict'

const on = (el, event, handler) => {
  if (el && event && handler) {
    el.addEventListener(event, handler, false)
  }
}

const off = (el, event, handler) => {
  if (el && event && handler) {
    el.removeEventListener(event, handler, false)
  }
}

export {
  on,
  off
}
