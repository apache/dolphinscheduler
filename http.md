# HTTP

If you need to use `Http script` for alerting, create an alert instance in the alert instance management and select the `Http` plugin.

Parameter Configuration
-----------------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">URL</td><td class="confluenceTd">The<span>&nbsp;</span><code>Http</code><span>&nbsp;</span>request URL needs to contain protocol, host, path and parameters if the method is<span>&nbsp;</span><code>GET.</code></td></tr><tr><td colspan="1" class="confluenceTd">Request Type</td><td colspan="1" class="confluenceTd">Select the request type from<span>&nbsp;</span><code>POST</code><span>&nbsp;</span>or<span>&nbsp;</span><code>GET.</code></td></tr><tr><td colspan="1" class="confluenceTd">Headers</td><td colspan="1" class="confluenceTd">The headers of the<span>&nbsp;</span><code>Http</code><span>&nbsp;</span>request in JSON format.</td></tr><tr><td class="confluenceTd">Body</td><td class="confluenceTd">The request body of the<span>&nbsp;</span><code>Http</code><span>&nbsp;</span>request in JSON format, when using<span>&nbsp;</span><code>POST</code><span>&nbsp;</span>method to alert.</td></tr><tr><td class="confluenceTd">Content Field</td><td class="confluenceTd">The field name to place the alert information.</td></tr></tbody></table>

Send Type
---------

Using `POST` and `GET` method to send `Http` request in the `Request Type`.

### GET HTTP

Send alert information by `HTTP` GET method. The following shows the `GET` configuration example:

![enterprise-wechat-app-msg-config](/img/alert/http-get-example.png)

### POST HTTP

Send alert information inside `Http` body by `HTTP` POST method. The following shows the `POST` configuration example:

![enterprise-wechat-app-msg-config](/img/alert/http-post-example.png)