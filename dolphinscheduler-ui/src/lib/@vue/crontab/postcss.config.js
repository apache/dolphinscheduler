module.exports = {
  plugins: [
    require('autoprefixer')({
      browsers: ["ie > 8", "last 2 version", "safari >= 9"]
    })
  ]
}
