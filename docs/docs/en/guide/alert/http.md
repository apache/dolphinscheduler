# HTTP

If you need to use `Http script` for alerting, create an alert instance in the alert instance management and select the `Http` plugin.

## Parameter Configuration

* URL
  > The `Http` request URL needs to contain protocol, host, path and parameters if the method is `GET`
* Request Type
  > Select the request type from `POST` or `GET`
* Headers
  > The headers of the `Http` request in JSON format
* Body
  > The request body of the `Http` request in JSON format, when using `POST` method to alert
* Content Field
  > The field name to place the alert information

## Send Type

Using `POST` and `GET` method to send `Http` request in the `Request Type`.

### GET Http

Send alert information by `Http` GET method.
The following shows the `GET` configuration example:

![enterprise-wechat-app-msg-config](/img/alert/http-get-example.png)

### POST Http

Send alert information inside `Http` body by `Http` POST method.
The following shows the `POST` configuration example:

![enterprise-wechat-app-msg-config](/img/alert/http-post-example.png)