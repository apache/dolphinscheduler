import Vue from 'vue'

const TIME_LIMIT = 500

const _nextTick = function (func, thisArg, ...args) {
  if (!func || typeof func !== 'function') return
  if (!func.startTime) {
    func.startTime = new Date()
  }
  if (new Date() - func.startTime > TIME_LIMIT) {
    func.startTime = null
  } else {
    Vue.nextTick(() => {
      func.apply(thisArg, args)
    })
  }
}

const _setTimeout = function (func, thisArg, timeout, ...args) {
  if (!func || typeof func !== 'function') return
  if (!func.startTime) {
    func.startTime = new Date()
  }
  if (new Date() - func.startTime > TIME_LIMIT) {
    func.startTime = null
  } else {
    setTimeout(() => {
      func.apply(thisArg, args)
    }, timeout)
  }
}

export default {
  nextTick: _nextTick,
  setTimeout: _setTimeout
}
