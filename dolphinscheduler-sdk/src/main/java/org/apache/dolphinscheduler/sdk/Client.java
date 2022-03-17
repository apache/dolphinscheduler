/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.sdk;

import org.apache.dolphinscheduler.sdk.tea.Common;
import org.apache.dolphinscheduler.sdk.tea.Tea;
import org.apache.dolphinscheduler.sdk.tea.TeaConverter;
import org.apache.dolphinscheduler.sdk.tea.TeaException;
import org.apache.dolphinscheduler.sdk.tea.TeaModel;
import org.apache.dolphinscheduler.sdk.tea.TeaPair;
import org.apache.dolphinscheduler.sdk.tea.TeaRequest;
import org.apache.dolphinscheduler.sdk.tea.TeaResponse;
import org.apache.dolphinscheduler.sdk.tea.TeaUnretryableException;

import java.io.InputStream;
import java.util.Map;

public class Client {

    public String _endpoint;
    public String _protocol;
    public String _userAgent;
    public Integer _readTimeout;
    public Integer _connectTimeout;
    public String _httpProxy;
    public String _httpsProxy;
    public String _noProxy;
    public Integer _maxIdleConns;
    public String _token;

    /**
     * Init client with Config
     * @param config config contains the necessary information to create a client
     */
    public Client(Config config)  {
        if (Common.isUnset(TeaModel.buildMap(config))) {
            throw new TeaException(TeaConverter.buildMap(
                    new TeaPair("code", "ParameterMissing"),
                    new TeaPair("message", "'config' can not be unset")
            ));
        }
        this._token = config.token;
        this._endpoint = config.endpoint;
        this._protocol = config.protocol;
        this._userAgent = config.userAgent;
        this._readTimeout = config.readTimeout;
        this._connectTimeout = config.connectTimeout;
        this._httpProxy = config.httpProxy;
        this._httpsProxy = config.httpsProxy;
        this._noProxy = config.noProxy;
        this._maxIdleConns = config.maxIdleConns;
    }

    public Map<String, ?> doRequest(TeaModel request, Params params) throws Exception {
        return this.doRequest(request, null, params, new RuntimeOptions());
    }

    public Map<String, ?> doRequest(TeaModel request, Params params, RuntimeOptions runtime) throws Exception {
        return this.doRequest(request, null, params, runtime);
    }

    public Map<String, ?> doRequest(TeaModel request, InputStream inputStream, Params params, RuntimeOptions runtime) throws Exception {
        Common.validateModel(request);
        Map<String, ?> map = request.toMap();
        OpenApiRequest req = OpenApiRequest.build(TeaConverter.buildMap(
                new TeaPair("body", map.get("body")),
                new TeaPair("headers", TeaConverter.merge(map.get("headers"))),
                new TeaPair("query", TeaConverter.merge(map.get("query")))
        ));
        if (!Common.isUnset(inputStream)) {
            req.stream = inputStream;
        }
        if (map.containsKey("path")) {
            params.pathname = Common.replace(params.pathname, TeaConverter.merge(map.get("path")));
        }
        Common.validateModel(params);
        return this.callApi(params, req, runtime);
    }

    public Map<String, ?> callApi(Params params, OpenApiRequest request, RuntimeOptions runtime) throws Exception{
        if (Common.isUnset(TeaModel.buildMap(params))) {
            throw new TeaException(TeaConverter.buildMap(
                    new TeaPair("code", "ParameterMissing"),
                    new TeaPair("message", "'params' can not be unset")
            ));
        }
        if (Common.equalString(params.style, "ROA") && Common.equalString(params.reqBodyType, "json")) {
            return this.doROARequest(params.action, params.version, params.protocol, params.method, params.authType, params.pathname, params.bodyType, request, runtime);
        } else if  (Common.equalString(params.style, "ROA") && Common.equalString(params.reqBodyType, "multiPartFormData")){
            return this.doROARequestWithMultiPartFormData(params.action, params.version, params.protocol, params.method, params.authType, params.pathname, params.bodyType, request, runtime);
        }
        else if (Common.equalString(params.style, "ROA")) {
            return this.doROARequestWithForm(params.action, params.version, params.protocol, params.method, params.authType, params.pathname, params.bodyType, request, runtime);
        }
        throw new TeaException(TeaConverter.buildMap(
                new TeaPair("code", "UnSupportStyle"),
                new TeaPair("message", String.format("'params.style' %s not supported", params.style))
        ));
    }

    public Map<String, ?> doROARequest(String action, String version, String protocol, String method, String authType, String pathname, String bodyType, OpenApiRequest request, RuntimeOptions runtime) throws Exception{
        TeaModel.validateParams(request, "request");
        Map<String, Object> runtime_ = TeaConverter.buildMap(
                new TeaPair("timeouted", "retry"),
                new TeaPair("readTimeout", Common.defaultNumber(runtime.readTimeout, _readTimeout)),
                new TeaPair("connectTimeout", Common.defaultNumber(runtime.connectTimeout, _connectTimeout)),
                new TeaPair("httpProxy", Common.defaultString(runtime.httpProxy, _httpProxy)),
                new TeaPair("httpsProxy", Common.defaultString(runtime.httpsProxy, _httpsProxy)),
                new TeaPair("noProxy", Common.defaultString(runtime.noProxy, _noProxy)),
                new TeaPair("maxIdleConns", Common.defaultNumber(runtime.maxIdleConns, _maxIdleConns)),
                new TeaPair("retry", TeaConverter.buildMap(
                        new TeaPair("retryable", runtime.autoretry),
                        new TeaPair("maxAttempts", Common.defaultNumber(runtime.maxAttempts, 3))
                )),
                new TeaPair("backoff", TeaConverter.buildMap(
                        new TeaPair("policy", Common.defaultString(runtime.backoffPolicy, "no")),
                        new TeaPair("period", Common.defaultNumber(runtime.backoffPeriod, 1))
                )),
                new TeaPair("ignoreSSL", runtime.ignoreSSL)
        );

        TeaRequest _lastRequest = null;
        Exception _lastException = null;
        long _now = System.currentTimeMillis();
        int _retryTimes = 0;
        while (Tea.allowRetry((Map<String, Object>) runtime_.get("retry"), _retryTimes, _now)) {
            if (_retryTimes > 0) {
                int backoffTime = Tea.getBackoffTime(runtime_.get("backoff"), _retryTimes);
                if (backoffTime > 0) {
                    Tea.sleep(backoffTime);
                }
            }
            _retryTimes = _retryTimes + 1;
            try {
                TeaRequest request_ = new TeaRequest();
                request_.protocol = Common.defaultString(_protocol, protocol);
                request_.method = method;
                request_.pathname = pathname;
                request_.headers = TeaConverter.merge(String.class,
                        TeaConverter.buildMap(
                                new TeaPair("date", Common.getDateUTCString()),
                                new TeaPair("host", _endpoint),
                                new TeaPair("accept", "application/json"),
                                new TeaPair("x-acs-signature-nonce", Common.getNonce()),
                                new TeaPair("x-acs-version", version),
                                new TeaPair("x-acs-action", action),
                                new TeaPair("user-agent", Common.getUserAgent(_userAgent))
                        ),
                        request.headers
                );
                if (!Common.isUnset(request.body)) {
                    request_.body = Tea.toReadable(Common.toJSONString(request.body));
                    request_.headers.put("content-type", "application/json; charset=utf-8");
                }

                if (!Common.isUnset(request.query)) {
                    request_.query = request.query;
                }

                if (Common.equalString(authType, "sts")) {
                    String token = this.getToken();
                    if (!Common.empty(token)) {
                        request_.headers.put("token", token);
                    }
                }

                _lastRequest = request_;
                TeaResponse response_ = Tea.doAction(request_, runtime_);

                if (Common.equalNumber(response_.statusCode, 204)) {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

                if (Common.is4xx(response_.statusCode) || Common.is5xx(response_.statusCode)) {
                    throw new TeaException(TeaConverter.buildMap(
                            new TeaPair("code",response_.statusCode),
                            new TeaPair("message",response_.statusMessage)
                    ));
                }

                if (Common.equalString(bodyType, "binary")) {
                    Map<String, Object> resp = TeaConverter.buildMap(
                            new TeaPair("body", response_.body),
                            new TeaPair("headers", response_.headers)
                    );
                    return resp;
                } else if (Common.equalString(bodyType, "byte")) {
                    byte[] byt = Common.readAsBytes(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", byt),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "string")) {
                    String str = Common.readAsString(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", str),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "json")) {
                    Object obj = Common.readAsJSON(response_.body);
                    Map<String, Object> res = Common.assertAsMap(obj);
                    return TeaConverter.buildMap(
                            new TeaPair("body", res),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "array")) {
                    Object arr = Common.readAsJSON(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", arr),
                            new TeaPair("headers", response_.headers)
                    );
                } else {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

            } catch (Exception e) {
                if (Tea.isRetryable(e)) {
                    _lastException = e;
                    continue;
                }
                throw e;
            }
        }

        throw new TeaUnretryableException(_lastRequest, _lastException);
    }

    public Map<String, ?> doROARequestWithForm(String action, String version, String protocol, String method, String authType, String pathname, String bodyType, OpenApiRequest request, RuntimeOptions runtime) throws Exception{
        TeaModel.validateParams(request, "request");
        Map<String, Object> runtime_ = TeaConverter.buildMap(
                new TeaPair("timeouted", "retry"),
                new TeaPair("readTimeout", Common.defaultNumber(runtime.readTimeout, _readTimeout)),
                new TeaPair("connectTimeout", Common.defaultNumber(runtime.connectTimeout, _connectTimeout)),
                new TeaPair("httpProxy", Common.defaultString(runtime.httpProxy, _httpProxy)),
                new TeaPair("httpsProxy", Common.defaultString(runtime.httpsProxy, _httpsProxy)),
                new TeaPair("noProxy", Common.defaultString(runtime.noProxy, _noProxy)),
                new TeaPair("maxIdleConns", Common.defaultNumber(runtime.maxIdleConns, _maxIdleConns)),
                new TeaPair("retry", TeaConverter.buildMap(
                        new TeaPair("retryable", runtime.autoretry),
                        new TeaPair("maxAttempts", Common.defaultNumber(runtime.maxAttempts, 3))
                )),
                new TeaPair("backoff", TeaConverter.buildMap(
                        new TeaPair("policy", Common.defaultString(runtime.backoffPolicy, "no")),
                        new TeaPair("period", Common.defaultNumber(runtime.backoffPeriod, 1))
                )),
                new TeaPair("ignoreSSL", runtime.ignoreSSL)
        );

        TeaRequest _lastRequest = null;
        Exception _lastException = null;
        long _now = System.currentTimeMillis();
        int _retryTimes = 0;
        while (Tea.allowRetry((Map<String, Object>) runtime_.get("retry"), _retryTimes, _now)) {
            if (_retryTimes > 0) {
                int backoffTime = Tea.getBackoffTime(runtime_.get("backoff"), _retryTimes);
                if (backoffTime > 0) {
                    Tea.sleep(backoffTime);
                }
            }
            _retryTimes = _retryTimes + 1;
            try {
                TeaRequest request_ = new TeaRequest();
                request_.protocol = Common.defaultString(_protocol, protocol);
                request_.method = method;
                request_.pathname = pathname;
                request_.headers = TeaConverter.merge(String.class,
                        TeaConverter.buildMap(
                                new TeaPair("date", Common.getDateUTCString()),
                                new TeaPair("host", _endpoint),
                                new TeaPair("accept", "application/json"),
                                new TeaPair("x-acs-signature-nonce", Common.getNonce()),
                                new TeaPair("x-acs-signature-method", "HMAC-SHA1"),
                                new TeaPair("x-acs-signature-version", "1.0"),
                                new TeaPair("x-acs-version", version),
                                new TeaPair("x-acs-action", action),
                                new TeaPair("user-agent", Common.getUserAgent(_userAgent))
                        ),
                        request.headers
                );
                if (!Common.isUnset(request.body)) {
                    Map<String, Object> m = Common.assertAsMap(request.body);
                    request_.body = Tea.toReadable(Common.toForm(m));
                    request_.headers.put("content-type", "application/x-www-form-urlencoded");
                }

                if (!Common.isUnset(request.query)) {
                    request_.query = request.query;
                }

                if (Common.equalString(authType, "sts")) {
                    String token = this.getToken();
                    if (!Common.empty(token)) {
                        request_.headers.put("token", token);
                    }
                }

                _lastRequest = request_;
                TeaResponse response_ = Tea.doAction(request_, runtime_);

                if (Common.equalNumber(response_.statusCode, 204)) {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

                if (Common.is4xx(response_.statusCode) || Common.is5xx(response_.statusCode)) {
                    throw new TeaException(TeaConverter.buildMap(
                            new TeaPair("code",response_.statusCode),
                            new TeaPair("message",response_.statusMessage)
                    ));
                }

                if (Common.equalString(bodyType, "binary")) {
                    Map<String, Object> resp = TeaConverter.buildMap(
                            new TeaPair("body", response_.body),
                            new TeaPair("headers", response_.headers)
                    );
                    return resp;
                } else if (Common.equalString(bodyType, "byte")) {
                    byte[] byt = Common.readAsBytes(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", byt),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "string")) {
                    String str = Common.readAsString(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", str),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "json")) {
                    Object obj = Common.readAsJSON(response_.body);
                    Map<String, Object> res = Common.assertAsMap(obj);
                    return TeaConverter.buildMap(
                            new TeaPair("body", res),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "array")) {
                    Object arr = Common.readAsJSON(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", arr),
                            new TeaPair("headers", response_.headers)
                    );
                } else {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

            } catch (Exception e) {
                if (Tea.isRetryable(e)) {
                    _lastException = e;
                    continue;
                }
                throw e;
            }
        }

        throw new TeaUnretryableException(_lastRequest, _lastException);
    }

    public Map<String, ?> doROARequestWithMultiPartFormData(String action, String version, String protocol, String method, String authType, String pathname, String bodyType, OpenApiRequest request, RuntimeOptions runtime) throws Exception{
        TeaModel.validateParams(request, "request");
        Map<String, Object> runtime_ = TeaConverter.buildMap(
                new TeaPair("timeouted", "retry"),
                new TeaPair("readTimeout", Common.defaultNumber(runtime.readTimeout, _readTimeout)),
                new TeaPair("connectTimeout", Common.defaultNumber(runtime.connectTimeout, _connectTimeout)),
                new TeaPair("httpProxy", Common.defaultString(runtime.httpProxy, _httpProxy)),
                new TeaPair("httpsProxy", Common.defaultString(runtime.httpsProxy, _httpsProxy)),
                new TeaPair("noProxy", Common.defaultString(runtime.noProxy, _noProxy)),
                new TeaPair("maxIdleConns", Common.defaultNumber(runtime.maxIdleConns, _maxIdleConns)),
                new TeaPair("retry", TeaConverter.buildMap(
                        new TeaPair("retryable", runtime.autoretry),
                        new TeaPair("maxAttempts", Common.defaultNumber(runtime.maxAttempts, 3))
                )),
                new TeaPair("backoff", TeaConverter.buildMap(
                        new TeaPair("policy", Common.defaultString(runtime.backoffPolicy, "no")),
                        new TeaPair("period", Common.defaultNumber(runtime.backoffPeriod, 1))
                )),
                new TeaPair("ignoreSSL", runtime.ignoreSSL)
        );

        TeaRequest _lastRequest = null;
        Exception _lastException = null;
        long _now = System.currentTimeMillis();
        int _retryTimes = 0;
        while (Tea.allowRetry((Map<String, Object>) runtime_.get("retry"), _retryTimes, _now)) {
            if (_retryTimes > 0) {
                int backoffTime = Tea.getBackoffTime(runtime_.get("backoff"), _retryTimes);
                if (backoffTime > 0) {
                    Tea.sleep(backoffTime);
                }
            }
            _retryTimes = _retryTimes + 1;
            try {
                TeaRequest request_ = new TeaRequest();
                request_.protocol = Common.defaultString(_protocol, protocol);
                request_.method = method;
                request_.pathname = pathname;
                request_.headers = TeaConverter.merge(String.class,
                        TeaConverter.buildMap(
                                new TeaPair("date", Common.getDateUTCString()),
                                new TeaPair("host", _endpoint),
                                new TeaPair("accept", "application/json"),
                                new TeaPair("x-acs-signature-nonce", Common.getNonce()),
                                new TeaPair("x-acs-version", version),
                                new TeaPair("x-acs-action", action),
                                new TeaPair("user-agent", Common.getUserAgent(_userAgent))
                        ),
                        request.headers
                );
                if (!Common.isUnset(request.stream)) {
                    request_.body = request.stream;
                    request_.headers.put("content-type", "multipart/form-data");
                }

                if (!Common.isUnset(request.query)) {
                    request_.query = request.query;
                }

                if (Common.equalString(authType, "sts")) {
                    String token = this.getToken();
                    if (!Common.empty(token)) {
                        request_.headers.put("token", token);
                    }
                }

                _lastRequest = request_;
                TeaResponse response_ = Tea.doMultipartFormData(request_, runtime_);

                if (Common.equalNumber(response_.statusCode, 204)) {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

                if (Common.is4xx(response_.statusCode) || Common.is5xx(response_.statusCode)) {
                    throw new TeaException(TeaConverter.buildMap(
                            new TeaPair("code",response_.statusCode),
                            new TeaPair("message",response_.statusMessage)
                    ));
                }

                if (Common.equalString(bodyType, "binary")) {
                    Map<String, Object> resp = TeaConverter.buildMap(
                            new TeaPair("body", response_.body),
                            new TeaPair("headers", response_.headers)
                    );
                    return resp;
                } else if (Common.equalString(bodyType, "byte")) {
                    byte[] byt = Common.readAsBytes(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", byt),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "string")) {
                    String str = Common.readAsString(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", str),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "json")) {
                    Object obj = Common.readAsJSON(response_.body);
                    Map<String, Object> res = Common.assertAsMap(obj);
                    return TeaConverter.buildMap(
                            new TeaPair("body", res),
                            new TeaPair("headers", response_.headers)
                    );
                } else if (Common.equalString(bodyType, "array")) {
                    Object arr = Common.readAsJSON(response_.body);
                    return TeaConverter.buildMap(
                            new TeaPair("body", arr),
                            new TeaPair("headers", response_.headers)
                    );
                } else {
                    return TeaConverter.buildMap(
                            new TeaPair("headers", response_.headers)
                    );
                }

            } catch (Exception e) {
                if (Tea.isRetryable(e)) {
                    _lastException = e;
                    continue;
                }
                throw e;
            }
        }

        throw new TeaUnretryableException(_lastRequest, _lastException);
    }

    /**
     * Get  token
     * @return  token
     */
    public String getToken() {
        if (Common.isUnset(_token)) {
            return "";
        }
        return _token;
    }
}

