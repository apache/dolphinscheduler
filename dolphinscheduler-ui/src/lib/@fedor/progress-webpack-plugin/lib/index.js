/**
 * Add by allex to display compilation progress
 */
'use strict';

var _ = require('lodash');
var webpack = require('webpack');
var istty = process.env.MSYSTEM === 'MINGW64' || !!process.stdout.isTTY;
var print = process.stderr.write.bind(process.stderr);

module.exports = function (options) {
  options = _.merge({ profile: true }, options);

  var chars = 0,
      lastState = void 0,
      lastStateTime = void 0,
      ts = void 0;

  return new webpack.ProgressPlugin(function (percentage, msg) {
    var state = msg;

    if (percentage === 0) {
      lastState = null;
      lastStateTime = +new Date();
      ts = lastStateTime;
    }

    if (percentage < 1) {
      msg = Math.floor(percentage * 100) + '% ' + msg;
      if (percentage < 1) {
        msg = ' ' + msg;
      }
      if (percentage < 0.1) {
        msg = ' ' + msg;
      }
    }

    if (options.profile) {
      state = state.replace(/^\d+\/\d+\s+/, '');
      if (state !== lastState || percentage === 1) {
        var now = +new Date();
        if (lastState) {
          var stateMsg = now - lastStateTime + 'ms ' + lastState;
          if (istty) {
            goToLineStart(stateMsg);
            print(stateMsg + '\n');
          } else {
            print('>');
          }
          chars = 0;
        }
        lastState = state;
        lastStateTime = now;
      }
    }

    if (istty) {
      goToLineStart(msg);
      print(msg);
    } else {
      if (Date.now() - ts > 200) {
        print('.');
        ts = Date.now();
      }
    }

    if (percentage === 1) {
      print('\n'); // completed.
    }
  });

  function goToLineStart(nextMessage) {
    var str = '';
    for (; chars > nextMessage.length; chars--) {
      str += '\b \b';
    }chars = nextMessage.length;
    for (var i = 0; i < chars; i++) {
      str += '\b';
    }if (str) print(str);
  }
};