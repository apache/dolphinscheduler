export default (fmt) => {
  if (/[HhmsS]/.test(fmt)) {
    return 'second'
  }

  if (/[Hhm]/.test(fmt)) {
    return 'minute'
  }

  if (/[Hh]/.test(fmt)) {
    return 'hour'
  }

  return false
}
