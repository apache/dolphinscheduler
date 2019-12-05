
export default () => {
  let date = new Date()
  return {
    year: date.getFullYear(),
    month: date.getMonth() + 1,
    today: date.getDate()
  }
}
