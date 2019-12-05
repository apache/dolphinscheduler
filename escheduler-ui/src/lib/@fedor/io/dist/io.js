/*!
 * io v1.0.5
 */

(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory(require('lodash')) :
	typeof define === 'function' && define.amd ? define(['lodash'], factory) :
	(global.io = factory(global._));
}(this, (function (lodash) { 'use strict';

lodash = lodash && lodash.hasOwnProperty('default') ? lodash['default'] : lodash;

'use strict';

var bind = function bind(fn, thisArg) {
  return function wrap() {
    var args = new Array(arguments.length);
    for (var i = 0; i < args.length; i++) {
      args[i] = arguments[i];
    }
    return fn.apply(thisArg, args);
  };
};

var isBuffer_1 = function (obj) {
  return obj != null && (isBuffer(obj) || isSlowBuffer(obj) || !!obj._isBuffer)
};

function isBuffer (obj) {
  return !!obj.constructor && typeof obj.constructor.isBuffer === 'function' && obj.constructor.isBuffer(obj)
}


function isSlowBuffer (obj) {
  return typeof obj.readFloatLE === 'function' && typeof obj.slice === 'function' && isBuffer(obj.slice(0, 0))
}

'use strict';





var toString = Object.prototype.toString;


function isArray(val) {
  return toString.call(val) === '[object Array]';
}


function isArrayBuffer(val) {
  return toString.call(val) === '[object ArrayBuffer]';
}


function isFormData(val) {
  return (typeof FormData !== 'undefined') && (val instanceof FormData);
}


function isArrayBufferView(val) {
  var result;
  if ((typeof ArrayBuffer !== 'undefined') && (ArrayBuffer.isView)) {
    result = ArrayBuffer.isView(val);
  } else {
    result = (val) && (val.buffer) && (val.buffer instanceof ArrayBuffer);
  }
  return result;
}


function isString(val) {
  return typeof val === 'string';
}


function isNumber(val) {
  return typeof val === 'number';
}


function isUndefined(val) {
  return typeof val === 'undefined';
}


function isObject(val) {
  return val !== null && typeof val === 'object';
}


function isDate(val) {
  return toString.call(val) === '[object Date]';
}


function isFile(val) {
  return toString.call(val) === '[object File]';
}


function isBlob(val) {
  return toString.call(val) === '[object Blob]';
}


function isFunction(val) {
  return toString.call(val) === '[object Function]';
}


function isStream(val) {
  return isObject(val) && isFunction(val.pipe);
}


function isURLSearchParams(val) {
  return typeof URLSearchParams !== 'undefined' && val instanceof URLSearchParams;
}


function trim(str) {
  return str.replace(/^\s*/, '').replace(/\s*$/, '');
}


function isStandardBrowserEnv() {
  if (typeof navigator !== 'undefined' && navigator.product === 'ReactNative') {
    return false;
  }
  return (
    typeof window !== 'undefined' &&
    typeof document !== 'undefined'
  );
}


function forEach(obj, fn) {

  if (obj === null || typeof obj === 'undefined') {
    return;
  }


  if (typeof obj !== 'object' && !isArray(obj)) {

    obj = [obj];
  }

  if (isArray(obj)) {

    for (var i = 0, l = obj.length; i < l; i++) {
      fn.call(null, obj[i], i, obj);
    }
  } else {

    for (var key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        fn.call(null, obj[key], key, obj);
      }
    }
  }
}


function merge(                           ) {
  var result = {};
  function assignValue(val, key) {
    if (typeof result[key] === 'object' && typeof val === 'object') {
      result[key] = merge(result[key], val);
    } else {
      result[key] = val;
    }
  }

  for (var i = 0, l = arguments.length; i < l; i++) {
    forEach(arguments[i], assignValue);
  }
  return result;
}


function extend(a, b, thisArg) {
  forEach(b, function assignValue(val, key) {
    if (thisArg && typeof val === 'function') {
      a[key] = bind(val, thisArg);
    } else {
      a[key] = val;
    }
  });
  return a;
}

var utils = {
  isArray: isArray,
  isArrayBuffer: isArrayBuffer,
  isBuffer: isBuffer_1,
  isFormData: isFormData,
  isArrayBufferView: isArrayBufferView,
  isString: isString,
  isNumber: isNumber,
  isObject: isObject,
  isUndefined: isUndefined,
  isDate: isDate,
  isFile: isFile,
  isBlob: isBlob,
  isFunction: isFunction,
  isStream: isStream,
  isURLSearchParams: isURLSearchParams,
  isStandardBrowserEnv: isStandardBrowserEnv,
  forEach: forEach,
  merge: merge,
  extend: extend,
  trim: trim
};

'use strict';



var normalizeHeaderName = function normalizeHeaderName(headers, normalizedName) {
  utils.forEach(headers, function processHeader(value, name) {
    if (name !== normalizedName && name.toUpperCase() === normalizedName.toUpperCase()) {
      headers[normalizedName] = value;
      delete headers[name];
    }
  });
};

'use strict';


var enhanceError = function enhanceError(error, config, code, request, response) {
  error.config = config;
  if (code) {
    error.code = code;
  }
  error.request = request;
  error.response = response;
  return error;
};

'use strict';




var createError = function createError(message, config, code, request, response) {
  var error = new Error(message);
  return enhanceError(error, config, code, request, response);
};

'use strict';




var settle = function settle(resolve, reject, response) {
  var validateStatus = response.config.validateStatus;

  if (!response.status || !validateStatus || validateStatus(response.status)) {
    resolve(response);
  } else {
    reject(createError(
      'Request failed with status code ' + response.status,
      response.config,
      null,
      response.request,
      response
    ));
  }
};

'use strict';



function encode(val) {
  return encodeURIComponent(val).
    replace(/%40/gi, '@').
    replace(/%3A/gi, ':').
    replace(/%24/g, '$').
    replace(/%2C/gi, ',').
    replace(/%20/g, '+').
    replace(/%5B/gi, '[').
    replace(/%5D/gi, ']');
}


var buildURL = function buildURL(url, params, paramsSerializer) {

  if (!params) {
    return url;
  }

  var serializedParams;
  if (paramsSerializer) {
    serializedParams = paramsSerializer(params);
  } else if (utils.isURLSearchParams(params)) {
    serializedParams = params.toString();
  } else {
    var parts = [];

    utils.forEach(params, function serialize(val, key) {
      if (val === null || typeof val === 'undefined') {
        return;
      }

      if (utils.isArray(val)) {
        key = key + '[]';
      }

      if (!utils.isArray(val)) {
        val = [val];
      }

      utils.forEach(val, function parseValue(v) {
        if (utils.isDate(v)) {
          v = v.toISOString();
        } else if (utils.isObject(v)) {
          v = JSON.stringify(v);
        }
        parts.push(encode(key) + '=' + encode(v));
      });
    });

    serializedParams = parts.join('&');
  }

  if (serializedParams) {
    url += (url.indexOf('?') === -1 ? '?' : '&') + serializedParams;
  }

  return url;
};

'use strict';




var parseHeaders = function parseHeaders(headers) {
  var parsed = {};
  var key;
  var val;
  var i;

  if (!headers) { return parsed; }

  utils.forEach(headers.split('\n'), function parser(line) {
    i = line.indexOf(':');
    key = utils.trim(line.substr(0, i)).toLowerCase();
    val = utils.trim(line.substr(i + 1));

    if (key) {
      parsed[key] = parsed[key] ? parsed[key] + ', ' + val : val;
    }
  });

  return parsed;
};

'use strict';



var isURLSameOrigin = (
  utils.isStandardBrowserEnv() ?


  (function standardBrowserEnv() {
    var msie = /(msie|trident)/i.test(navigator.userAgent);
    var urlParsingNode = document.createElement('a');
    var originURL;


    function resolveURL(url) {
      var href = url;

      if (msie) {

        urlParsingNode.setAttribute('href', href);
        href = urlParsingNode.href;
      }

      urlParsingNode.setAttribute('href', href);


      return {
        href: urlParsingNode.href,
        protocol: urlParsingNode.protocol ? urlParsingNode.protocol.replace(/:$/, '') : '',
        host: urlParsingNode.host,
        search: urlParsingNode.search ? urlParsingNode.search.replace(/^\?/, '') : '',
        hash: urlParsingNode.hash ? urlParsingNode.hash.replace(/^#/, '') : '',
        hostname: urlParsingNode.hostname,
        port: urlParsingNode.port,
        pathname: (urlParsingNode.pathname.charAt(0) === '/') ?
                  urlParsingNode.pathname :
                  '/' + urlParsingNode.pathname
      };
    }

    originURL = resolveURL(window.location.href);


    return function isURLSameOrigin(requestURL) {
      var parsed = (utils.isString(requestURL)) ? resolveURL(requestURL) : requestURL;
      return (parsed.protocol === originURL.protocol &&
            parsed.host === originURL.host);
    };
  })() :


  (function nonStandardBrowserEnv() {
    return function isURLSameOrigin() {
      return true;
    };
  })()
);

'use strict';


var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=';

function E() {
  this.message = 'String contains an invalid character';
}
E.prototype = new Error;
E.prototype.code = 5;
E.prototype.name = 'InvalidCharacterError';

function btoa$1(input) {
  var str = String(input);
  var output = '';
  for (

    var block, charCode, idx = 0, map = chars;


    str.charAt(idx | 0) || (map = '=', idx % 1);

    output += map.charAt(63 & block >> 8 - idx % 1 * 8)
  ) {
    charCode = str.charCodeAt(idx += 3 / 4);
    if (charCode > 0xFF) {
      throw new E();
    }
    block = block << 8 | charCode;
  }
  return output;
}

var btoa_1 = btoa$1;

'use strict';



var cookies = (
  utils.isStandardBrowserEnv() ?


  (function standardBrowserEnv() {
    return {
      write: function write(name, value, expires, path, domain, secure) {
        var cookie = [];
        cookie.push(name + '=' + encodeURIComponent(value));

        if (utils.isNumber(expires)) {
          cookie.push('expires=' + new Date(expires).toGMTString());
        }

        if (utils.isString(path)) {
          cookie.push('path=' + path);
        }

        if (utils.isString(domain)) {
          cookie.push('domain=' + domain);
        }

        if (secure === true) {
          cookie.push('secure');
        }

        document.cookie = cookie.join('; ');
      },

      read: function read(name) {
        var match = document.cookie.match(new RegExp('(^|;\\s*)(' + name + ')=([^;]*)'));
        return (match ? decodeURIComponent(match[3]) : null);
      },

      remove: function remove(name) {
        this.write(name, '', Date.now() - 86400000);
      }
    };
  })() :


  (function nonStandardBrowserEnv() {
    return {
      write: function write() {},
      read: function read() { return null; },
      remove: function remove() {}
    };
  })()
);

'use strict';







var btoa = (typeof window !== 'undefined' && window.btoa && window.btoa.bind(window)) || btoa_1;

var xhr = function xhrAdapter(config) {
  return new Promise(function dispatchXhrRequest(resolve, reject) {
    var requestData = config.data;
    var requestHeaders = config.headers;

    if (utils.isFormData(requestData)) {
      delete requestHeaders['Content-Type'];
    }

    var request = new XMLHttpRequest();
    var loadEvent = 'onreadystatechange';
    var xDomain = false;


    if (process.env.NODE_ENV !== 'test' &&
        typeof window !== 'undefined' &&
        window.XDomainRequest && !('withCredentials' in request) &&
        !isURLSameOrigin(config.url)) {
      request = new window.XDomainRequest();
      loadEvent = 'onload';
      xDomain = true;
      request.onprogress = function handleProgress() {};
      request.ontimeout = function handleTimeout() {};
    }


    if (config.auth) {
      var username = config.auth.username || '';
      var password = config.auth.password || '';
      requestHeaders.Authorization = 'Basic ' + btoa(username + ':' + password);
    }

    request.open(config.method.toUpperCase(), buildURL(config.url, config.params, config.paramsSerializer), true);


    request.timeout = config.timeout;


    request[loadEvent] = function handleLoad() {
      if (!request || (request.readyState !== 4 && !xDomain)) {
        return;
      }


      if (request.status === 0 && !(request.responseURL && request.responseURL.indexOf('file:') === 0)) {
        return;
      }


      var responseHeaders = 'getAllResponseHeaders' in request ? parseHeaders(request.getAllResponseHeaders()) : null;
      var responseData = !config.responseType || config.responseType === 'text' ? request.responseText : request.response;
      var response = {
        data: responseData,

        status: request.status === 1223 ? 204 : request.status,
        statusText: request.status === 1223 ? 'No Content' : request.statusText,
        headers: responseHeaders,
        config: config,
        request: request
      };

      settle(resolve, reject, response);


      request = null;
    };


    request.onerror = function handleError() {


      reject(createError('Network Error', config, null, request));


      request = null;
    };


    request.ontimeout = function handleTimeout() {
      reject(createError('timeout of ' + config.timeout + 'ms exceeded', config, 'ECONNABORTED',
        request));


      request = null;
    };


    if (utils.isStandardBrowserEnv()) {
      var cookies$$1 = cookies;


      var xsrfValue = (config.withCredentials || isURLSameOrigin(config.url)) && config.xsrfCookieName ?
          cookies$$1.read(config.xsrfCookieName) :
          undefined;

      if (xsrfValue) {
        requestHeaders[config.xsrfHeaderName] = xsrfValue;
      }
    }


    if ('setRequestHeader' in request) {
      utils.forEach(requestHeaders, function setRequestHeader(val, key) {
        if (typeof requestData === 'undefined' && key.toLowerCase() === 'content-type') {

          delete requestHeaders[key];
        } else {

          request.setRequestHeader(key, val);
        }
      });
    }


    if (config.withCredentials) {
      request.withCredentials = true;
    }


    if (config.responseType) {
      try {
        request.responseType = config.responseType;
      } catch (e) {


        if (config.responseType !== 'json') {
          throw e;
        }
      }
    }


    if (typeof config.onDownloadProgress === 'function') {
      request.addEventListener('progress', config.onDownloadProgress);
    }


    if (typeof config.onUploadProgress === 'function' && request.upload) {
      request.upload.addEventListener('progress', config.onUploadProgress);
    }

    if (config.cancelToken) {

      config.cancelToken.promise.then(function onCanceled(cancel) {
        if (!request) {
          return;
        }

        request.abort();
        reject(cancel);

        request = null;
      });
    }

    if (requestData === undefined) {
      requestData = null;
    }


    request.send(requestData);
  });
};

'use strict';




var DEFAULT_CONTENT_TYPE = {
  'Content-Type': 'application/x-www-form-urlencoded'
};

function setContentTypeIfUnset(headers, value) {
  if (!utils.isUndefined(headers) && utils.isUndefined(headers['Content-Type'])) {
    headers['Content-Type'] = value;
  }
}

function getDefaultAdapter() {
  var adapter;
  if (typeof XMLHttpRequest !== 'undefined') {

    adapter = xhr;
  } else if (typeof process !== 'undefined') {

    adapter = xhr;
  }
  return adapter;
}

var defaults = {
  adapter: getDefaultAdapter(),

  transformRequest: [function transformRequest(data, headers) {
    normalizeHeaderName(headers, 'Content-Type');
    if (utils.isFormData(data) ||
      utils.isArrayBuffer(data) ||
      utils.isBuffer(data) ||
      utils.isStream(data) ||
      utils.isFile(data) ||
      utils.isBlob(data)
    ) {
      return data;
    }
    if (utils.isArrayBufferView(data)) {
      return data.buffer;
    }
    if (utils.isURLSearchParams(data)) {
      setContentTypeIfUnset(headers, 'application/x-www-form-urlencoded;charset=utf-8');
      return data.toString();
    }
    if (utils.isObject(data)) {
      setContentTypeIfUnset(headers, 'application/json;charset=utf-8');
      return JSON.stringify(data);
    }
    return data;
  }],

  transformResponse: [function transformResponse(data) {

    if (typeof data === 'string') {
      try {
        data = JSON.parse(data);
      } catch (e) {              }
    }
    return data;
  }],

  timeout: 0,

  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',

  maxContentLength: -1,

  validateStatus: function validateStatus(status) {
    return status >= 200 && status < 300;
  }
};

defaults.headers = {
  common: {
    'Accept': 'application/json, text/plain, */*'
  }
};

utils.forEach(['delete', 'get', 'head'], function forEachMethodNoData(method) {
  defaults.headers[method] = {};
});

utils.forEach(['post', 'put', 'patch'], function forEachMethodWithData(method) {
  defaults.headers[method] = utils.merge(DEFAULT_CONTENT_TYPE);
});

var defaults_1 = defaults;

'use strict';



function InterceptorManager() {
  this.handlers = [];
}


InterceptorManager.prototype.use = function use(fulfilled, rejected) {
  this.handlers.push({
    fulfilled: fulfilled,
    rejected: rejected
  });
  return this.handlers.length - 1;
};


InterceptorManager.prototype.eject = function eject(id) {
  if (this.handlers[id]) {
    this.handlers[id] = null;
  }
};


InterceptorManager.prototype.forEach = function forEach(fn) {
  utils.forEach(this.handlers, function forEachHandler(h) {
    if (h !== null) {
      fn(h);
    }
  });
};

var InterceptorManager_1 = InterceptorManager;

'use strict';




var transformData = function transformData(data, headers, fns) {

  utils.forEach(fns, function transform(fn) {
    data = fn(data, headers);
  });

  return data;
};

'use strict';

var isCancel = function isCancel(value) {
  return !!(value && value.__CANCEL__);
};

'use strict';







function throwIfCancellationRequested(config) {
  if (config.cancelToken) {
    config.cancelToken.throwIfRequested();
  }
}


var dispatchRequest = function dispatchRequest(config) {
  throwIfCancellationRequested(config);


  config.headers = config.headers || {};


  config.data = transformData(
    config.data,
    config.headers,
    config.transformRequest
  );


  config.headers = utils.merge(
    config.headers.common || {},
    config.headers[config.method] || {},
    config.headers || {}
  );

  utils.forEach(
    ['delete', 'get', 'head', 'post', 'put', 'patch', 'common'],
    function cleanHeaderConfig(method) {
      delete config.headers[method];
    }
  );

  var adapter = config.adapter || defaults_1.adapter;

  return adapter(config).then(function onAdapterResolution(response) {
    throwIfCancellationRequested(config);


    response.data = transformData(
      response.data,
      response.headers,
      config.transformResponse
    );

    return response;
  }, function onAdapterRejection(reason) {
    if (!isCancel(reason)) {
      throwIfCancellationRequested(config);


      if (reason && reason.response) {
        reason.response.data = transformData(
          reason.response.data,
          reason.response.headers,
          config.transformResponse
        );
      }
    }

    return Promise.reject(reason);
  });
};

'use strict';


var isAbsoluteURL = function isAbsoluteURL(url) {


  return /^([a-z][a-z\d\+\-\.]*:)?\/\//i.test(url);
};

'use strict';


var combineURLs = function combineURLs(baseURL, relativeURL) {
  return relativeURL
    ? baseURL.replace(/\/+$/, '') + '/' + relativeURL.replace(/^\/+/, '')
    : baseURL;
};

'use strict';









function Axios(instanceConfig) {
  this.defaults = instanceConfig;
  this.interceptors = {
    request: new InterceptorManager_1(),
    response: new InterceptorManager_1()
  };
}


Axios.prototype.request = function request(config) {


  if (typeof config === 'string') {
    config = utils.merge({
      url: arguments[0]
    }, arguments[1]);
  }

  config = utils.merge(defaults_1, this.defaults, { method: 'get' }, config);
  config.method = config.method.toLowerCase();


  if (config.baseURL && !isAbsoluteURL(config.url)) {
    config.url = combineURLs(config.baseURL, config.url);
  }


  var chain = [dispatchRequest, undefined];
  var promise = Promise.resolve(config);

  this.interceptors.request.forEach(function unshiftRequestInterceptors(interceptor) {
    chain.unshift(interceptor.fulfilled, interceptor.rejected);
  });

  this.interceptors.response.forEach(function pushResponseInterceptors(interceptor) {
    chain.push(interceptor.fulfilled, interceptor.rejected);
  });

  while (chain.length) {
    promise = promise.then(chain.shift(), chain.shift());
  }

  return promise;
};


utils.forEach(['delete', 'get', 'head', 'options'], function forEachMethodNoData(method) {

  Axios.prototype[method] = function(url, config) {
    return this.request(utils.merge(config || {}, {
      method: method,
      url: url
    }));
  };
});

utils.forEach(['post', 'put', 'patch'], function forEachMethodWithData(method) {

  Axios.prototype[method] = function(url, data, config) {
    return this.request(utils.merge(config || {}, {
      method: method,
      url: url,
      data: data
    }));
  };
});

var Axios_1 = Axios;

'use strict';


function Cancel(message) {
  this.message = message;
}

Cancel.prototype.toString = function toString() {
  return 'Cancel' + (this.message ? ': ' + this.message : '');
};

Cancel.prototype.__CANCEL__ = true;

var Cancel_1 = Cancel;

'use strict';




function CancelToken(executor) {
  if (typeof executor !== 'function') {
    throw new TypeError('executor must be a function.');
  }

  var resolvePromise;
  this.promise = new Promise(function promiseExecutor(resolve) {
    resolvePromise = resolve;
  });

  var token = this;
  executor(function cancel(message) {
    if (token.reason) {

      return;
    }

    token.reason = new Cancel_1(message);
    resolvePromise(token.reason);
  });
}


CancelToken.prototype.throwIfRequested = function throwIfRequested() {
  if (this.reason) {
    throw this.reason;
  }
};


CancelToken.source = function source() {
  var cancel;
  var token = new CancelToken(function executor(c) {
    cancel = c;
  });
  return {
    token: token,
    cancel: cancel
  };
};

var CancelToken_1 = CancelToken;

'use strict';


var spread = function spread(callback) {
  return function wrap(arr) {
    return callback.apply(null, arr);
  };
};

'use strict';







function createInstance(defaultConfig) {
  var context = new Axios_1(defaultConfig);
  var instance = bind(Axios_1.prototype.request, context);


  utils.extend(instance, Axios_1.prototype, context);


  utils.extend(instance, context);

  return instance;
}


var axios$2 = createInstance(defaults_1);


axios$2.Axios = Axios_1;


axios$2.create = function create(instanceConfig) {
  return createInstance(utils.merge(defaults_1, instanceConfig));
};


axios$2.Cancel = Cancel_1;
axios$2.CancelToken = CancelToken_1;
axios$2.isCancel = isCancel;


axios$2.all = function all(promises) {
  return Promise.all(promises);
};
axios$2.spread = spread;

var axios_1 = axios$2;


var default_1 = axios$2;

axios_1.default = default_1;

var axios = axios_1;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var param = function param(a) {
  var s = [],
      rbracket = /\[\]$/,
      isArray = function isArray(obj) {
    return Object.prototype.toString.call(obj) === '[object Array]';
  },
      add = function add(k, v) {
    v = typeof v === 'function' ? v() : v === null ? '' : v === undefined ? '' : v;
    s[s.length] = encodeURIComponent(k) + '=' + encodeURIComponent(v);
  },
      buildParams = function buildParams(prefix, obj) {
    var i, len, key;

    if (prefix) {
      if (isArray(obj)) {
        for (i = 0, len = obj.length; i < len; i++) {
          if (rbracket.test(prefix)) {
            add(prefix, obj[i]);
          } else {
            buildParams(prefix + '[' + (_typeof(obj[i]) === 'object' ? i : '') + ']', obj[i]);
          }
        }
      } else if (obj && String(obj) === '[object Object]') {
        for (key in obj) {
          buildParams(prefix + '[' + key + ']', obj[key]);
        }
      } else {
        add(prefix, obj);
      }
    } else if (isArray(obj)) {
      for (i = 0, len = obj.length; i < len; i++) {
        add(obj[i].name, obj[i].value);
      }
    } else {
      for (key in obj) {
        buildParams(key, obj[key]);
      }
    }
    return s;
  };
  return buildParams('', a).join('&').replace(/%20/g, '+');
};

var querystring = param;

var jsonp_1 = jsonp;

var count = 0;

function noop() {}

function jsonp(url, opts, fn) {
  if (typeof opts === 'function') {
    fn = opts;
    opts = {};
  }
  if (!opts) opts = {};

  var prefix = opts.prefix || '__jp';

  var id = opts.name || prefix + count++;

  var param = opts.param || 'callback';
  var timeout = opts.timeout != null ? opts.timeout : 60000;
  var enc = encodeURIComponent;

  var target = document.getElementsByTagName('script')[0] || document.head;
  var script;
  var timer;

  if (timeout) {
    timer = setTimeout(function () {
      cleanup();
      if (fn) fn(new Error('Timeout'));
    }, timeout);
  }

  function cleanup() {
    script.onerror = null;
    script.onload = null;

    if (script.parentNode) script.parentNode.removeChild(script);
    window[id] = noop;

    if (timer) clearTimeout(timer);
  }

  function cancel() {
    if (window[id]) {
      cleanup();
    }
  }

  window[id] = function (data) {
    cleanup();
    if (fn) fn(null, data);
  };

  url += (~url.indexOf('?') ? '&' : '?') + param + '=' + enc(id);
  url = url.replace('?&', '?');

  var handler = function handler(_ref) {
    var type = _ref.type;

    if (type === 'error') {
      cleanup();
      fn(new Error('http error'));
    }
  };

  script = document.createElement('script');
  script.src = url;
  script.onload = handler;
  script.onerror = handler;
  target.parentNode.insertBefore(script, target);

  return cancel;
}

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }









var preflightDataMethods = ['post', 'put', 'patch'];
var API_ASSERT_OK = 0;

var def = function def(o, p, v, desc) {
  return Object.defineProperty(o, p, Object.assign({ writable: false, enumerable: false, configurable: false }, desc, { value: v }));
};

var normalizeArgs = function normalizeArgs(method, url, data, success, fail, config) {
  if (lodash.isFunction(data)) {
    config = fail;
    fail = success;
    success = data;
  }
  if (lodash.isPlainObject(data)) {
    if (!lodash.includes(preflightDataMethods, method)) {
      config = lodash.merge({}, config, { params: data });
    } else {
      config = lodash.merge({}, config, { data: data });
    }
  } else {
    config = config || {};
  }
  config.method = method;
  config.url = url;
  return {
    success: success, fail: fail, config: config
  };
};

var generalHandle = function generalHandle(data, res, resolve, reject, success, fail) {
  if (!data || +(data.code || 0) !== API_ASSERT_OK) {
    fail && fail(data);
    reject(data);
  } else {
    success && success(data);
    resolve(data);
  }
};

var isAbsUrl = function isAbsUrl(url) {
  return (/^(https?:)?\/\//i.test(url)
  );
};

var resolveURL = function resolveURL(base, path) {
  if (!base || path && isAbsUrl(path)) {
    return path;
  }
  return combineURLs(base, path);
};

var create = function create(cfg) {
  return new InnerCtor(cfg);
};

var InnerCtor = function () {
  function InnerCtor(defaults) {
    var _this = this;

    _classCallCheck(this, InnerCtor);

    var inter = axios.create(defaults);

    this.config = Object.assign({
      baseURL: '',
      timeout: 0,
      resolveURL: function resolveURL(u) {
        return u;
      }
    }, defaults);

    this.inter = inter;
    this.interceptors = inter.interceptors;

    this.jsonp = this.jsonp.bind(this);

    this.jsonp.inter = jsonp_1;['get', 'delete', 'head', 'options', 'post', 'put', 'patch'].forEach(function (method) {
      _this[method] = function (url, data, success, fail, config) {
        return this.request({ url: url, method: method, data: data, success: success, fail: fail, config: config });
      }.bind(_this);
    });
  }

  InnerCtor.prototype.request = function request(_ref) {
    var _this2 = this;

    var url = _ref.url,
        method = _ref.method,
        data = _ref.data,
        success = _ref.success,
        fail = _ref.fail,
        config = _ref.config;

    var configs = normalizeArgs(method, this.config.resolveURL(url), data, success, fail, config);
    configs.config = lodash.merge({}, this.config, configs.config);

    if (configs.config.emulateJSON !== false) {
      configs.config.data = querystring(configs.config.data);
    }

    return new Promise(function (resolve, reject) {
      _this2.inter.request(configs.config).then(function (res) {
        if (method === 'head' || method === 'options') {
          res.data = res.headers;
        }
        generalHandle(res.data, res, resolve, reject, configs.success, configs.fail);
      }).catch(function (err) {
        var ret = void 0,
            code = void 0;

        if (err.response && err.response.status) {
          code = err.response.status;
        } else {
          code = 500;
        }
        if (err.response && (method === 'head' || method === 'options')) {
          err.response.data = err.response.headers;
        }

        if (err.response && err.response.data) {
          if (lodash.isString(err.response.data)) {
            ret = {
              message: err.message,
              code: code,
              data: err.response.data
            };
          } else {
            ret = err.response.data;
          }
        } else {
          ret = {
            code: code,
            message: err.message,
            data: null
          };
        }
        def(ret, '$error', err);
        reject(ret);
      });
    });
  };

  InnerCtor.prototype.jsonp = function jsonp(url, data, success, fail, config) {
    var configs = normalizeArgs('jsonp', this.config.resolveURL(url), data, success, fail, config);

    configs.config = lodash.merge({}, this.config, configs.config);
    configs.url = buildURL(resolveURL(configs.config.baseURL, configs.config.url), configs.config.params);

    return new Promise(function (resolve, reject) {
      jsonp_1(configs.url, configs.config, function (err, data) {
        if (err) {
          var ret = {
            code: 500,
            message: err.message,
            data: null
          };
          def(ret, '$error', err);
          reject(ret);
        } else {
          generalHandle(data, data, resolve, reject, configs.success, configs.fail);
        }
      });
    });
  };

  return InnerCtor;
}();

var src = Object.assign(create({}), { create: create, axios: axios });

return src;

})));
