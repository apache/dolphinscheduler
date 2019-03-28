require(['gitbook', 'jQuery'], function (gitbook, $) {
  var url = ''
  var src = ''

  var insertLogo = function (src, url) {
    $('.book-summary').children().eq(0).before('<a class="book-logo" href="' + url + '"><img src="' + src + '"</a>')
  }
  gitbook.events.bind('start', function (e, config) {
    src = config['insert-logo-link']['src']
    url = config['insert-logo-link']['url']
  })

  gitbook.events.bind("page.change", function() {
    insertLogo(src, url)
  })
})
