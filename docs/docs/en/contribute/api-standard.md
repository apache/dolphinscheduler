# API design standard

A standardized and unified API is the cornerstone of project design.The API of DolphinScheduler follows the REST ful standard. REST ful is currently the most popular Internet software architecture. It has a clear structure, conforms to standards, is easy to understand and extend.

This article uses the DolphinScheduler API as an example to explain how to construct a Restful API.

## 1. URI design

REST is "Representational State Transfer".The design of Restful URI is based on resources.The resource corresponds to an entity on the network, for example: a piece of text, a picture, and a service. And each resource corresponds to a URI.

+ One Kind of Resource: expressed in the plural, such as `task-instances`、`groups` ;
+ A Resource: expressed in the singular, or use the ID to represent the corresponding resource, such as `group`、`groups/{groupId}`;
+ Sub Resources: Resources under a certain resource, such as `/instances/{instanceId}/tasks`;
+ A Sub Resource：`/instances/{instanceId}/tasks/{taskId}`;

## 2. Method design

We need to locate a certain resource by URI, and then use Method or declare actions in the path suffix to reflect the operation of the resource.

### ① Query - GET

Use URI to locate the resource, and use GET to indicate query.

+ When the URI is a type of resource, it means to query a type of resource. For example, the following example indicates paging query `alter-groups`.

```
Method: GET
/dolphinscheduler/alert-groups
```

+ When the URI is a single resource, it means to query this resource. For example, the following example means to query the specified `alter-group`.

```
Method: GET
/dolphinscheduler/alter-groups/{id}
```

+ In addition, we can also express query sub-resources based on URI, as follows:

```
Method: GET
/dolphinscheduler/projects/{projectId}/tasks
```

**The above examples all represent paging query. If we need to query all data, we need to add `/list` after the URI to distinguish. Do not mix the same API for both paged query and query.**

```
Method: GET
/dolphinscheduler/alert-groups/list
```

### ② Create - POST

Use URI to locate the resource, use POST to indicate create, and then return the created id to requester.

+ create an `alter-group`：

```
Method: POST
/dolphinscheduler/alter-groups
```

+ create sub-resources is also the same as above.

```
Method: POST
/dolphinscheduler/alter-groups/{alterGroupId}/tasks
```

### ③ Modify - PUT

Use URI to locate the resource, use PUT to indicate modify.
+ modify an `alert-group`

```
Method: PUT
/dolphinscheduler/alter-groups/{alterGroupId}
```

### ④ Delete -DELETE

Use URI to locate the resource, use DELETE to indicate delete.

+ delete an `alert-group`

```
Method: DELETE
/dolphinscheduler/alter-groups/{alterGroupId}
```

+ batch deletion: batch delete the id array，we should use POST. **（Do not use the DELETE method, because the body of the DELETE request has no semantic meaning, and it is possible that some gateways, proxies, and firewalls will directly strip off the request body after receiving the DELETE request.）**

```
Method: POST
/dolphinscheduler/alter-groups/batch-delete
```

### ⑤ Partial Modifications -PATCH

Use URI to locate the resource, use PATCH to partial modifications.

```
Method: PATCH
/dolphinscheduler/alter-groups/{alterGroupId}
```

### ⑥ Others

In addition to creating, deleting, modifying and querying, we also locate the corresponding resource through url, and then append operations to it after the path, such as:

```
/dolphinscheduler/alert-groups/verify-name
/dolphinscheduler/projects/{projectCode}/process-instances/{code}/view-gantt
```

## 3. Parameter design

There are two types of parameters, one is request parameter and the other is path parameter. And the parameter must use small hump.

In the case of paging, if the parameter entered by the user is less than 1, the front end needs to automatically turn to 1, indicating that the first page is requested; When the backend finds that the parameter entered by the user is greater than the total number of pages, it should directly return to the last page.

## 4. Others design

### base URL

The URI of the project needs to use `/<project_name>` as the base path, so as to identify that these APIs are under this project.

```
/dolphinscheduler
```

