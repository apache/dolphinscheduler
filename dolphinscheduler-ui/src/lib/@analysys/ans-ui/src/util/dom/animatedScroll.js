const requestAnimFrame = (function () {
  return window.requestAnimationFrame ||
    window.webkitRequestAnimationFrame ||
    window.mozRequestAnimationFrame ||
    function (callback) {
      window.setTimeout(callback, 1000 / 60)
    }
})()

const linear = function (t, b, c, d) {
  return t / d * c + b
}

const scrollBy = function (element, change, vertical, duration, callback) {
  let start = vertical ? element.scrollTop : element.scrollLeft
  let to = start + change
  if (element.__scroll__controller && element.__scroll__controller.isAnimating()) {
    to = element.__scroll__controller.to + change
    element.__scroll__controller.stop()
  }

  const controller = _scrollTo(element, start, to, vertical, duration, callback)
  element.__scroll__controller = controller
  return controller
}

const scrollTo = function (element, to, vertical, duration, callback) {
  let start = vertical ? element.scrollTop : element.scrollLeft
  if (element.__scroll__controller && element.__scroll__controller.isAnimating()) {
    element.__scroll__controller.stop()
  }

  const controller = _scrollTo(element, start, to, vertical, duration, callback)
  element.__scroll__controller = controller
  return controller
}

function _scrollTo (element, start, to, vertical, duration = 200, callback) {
  const change = to - start
  const animationStart = +new Date()
  let animating = true
  let lastValue = null

  const animateScroll = function () {
    if (!animating) {
      return
    }
    requestAnimFrame(animateScroll)
    const now = +new Date()
    const val = Math.floor(linear(now - animationStart, start, change, duration))
    if (lastValue) {
      const targetValue = vertical ? element.scrollTop : element.scrollLeft
      if (lastValue === targetValue) {
        lastValue = val
        vertical ? (element.scrollTop = val) : (element.scrollLeft = val)
      } else {
        animating = false
      }
    } else {
      lastValue = val
      vertical ? (element.scrollTop = val) : (element.scrollLeft = val)
    }
    if (now > animationStart + duration) {
      vertical ? (element.scrollTop = to) : (element.scrollLeft = to)
      animating = false
      if (callback) { callback() }
    }
  }
  requestAnimFrame(animateScroll)

  const maxScroll = vertical
    ? element.scrollHeight - element.offsetHeight
    : element.scrollWidth - element.offsetWidth
  const validTo = Math.min(Math.max(to, 0), maxScroll)

  return {
    to: validTo,
    isAnimating () {
      return animating
    },
    stop () {
      animating = false
    }
  }
}

export default {
  scrollTo,
  scrollBy
}
