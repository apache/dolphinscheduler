# Flink Calls Operating Steps

## Create a Queue

1. Log in to the scheduling system, click `Security`, then click `Queue manage` on the left, and click `Create queue` to create a queue.
2. Fill in the name and value of the queue, and click "Submit" 

<p align="center">
   <img src="/img/api/create_queue.png" width="80%" />
 </p>

## Create a Tenant 

```
1. The tenant corresponds to a Linux user, which the user worker uses to submit jobs. If the Linux OS environment does not have this user, the worker will create this user when executing the script.
2. Both the tenant and the tenant code are unique and cannot be repeated, just like a person only has one name and one ID number.  
3. After creating a tenant, there will be a folder in the HDFS relevant directory.  
```

<p align="center">
   <img src="/img/api/create_tenant.png" width="80%" />
 </p>

## Create a User

<p align="center">
   <img src="/img/api/create_user.png" width="80%" />
 </p>

## Create a Token

1. Log in to the scheduling system, click `Security`, then click `Token manage` on the left, and click `Create token` to create a token.

<p align="center">
   <img src="/img/token-management-en.png" width="80%" />
 </p>


2. Select the `Expiration time` (token validity time), select `User` (choose the specified user to perform the API operation), click "Generate token", copy the `Token` string, and click "Submit".

<p align="center">
   <img src="/img/create-token-en1.png" width="80%" />
 </p>

## Token Usage

1. Open the API documentation page

   > Address：http://{api server ip}:12345/dolphinscheduler/doc.html?language=en_US&lang=en

<p align="center">
   <img src="/img/api-documentation-en.png" width="80%" />
 </p>


2. Select a test API, the API selected for this test is `queryAllProjectList`

   > projects/query-project-list

3. Open `Postman`, fill in the API address, and enter the `Token` in `Headers`, and then send the request to view the result:

   ```
   token: The Token just generated
   ```

<p align="center">
   <img src="/img/test-api.png" width="80%" />
 </p>  

## User Authorization

<p align="center">
   <img src="/img/api/user_authorization.png" width="80%" />
 </p>

## User Login

```
http://192.168.1.163:12345/dolphinscheduler/ui/#/monitor/servers/master
```

<p align="center">
   <img src="/img/api/user_login.png" width="80%" />
 </p>

## Upload the Resource

<p align="center">
   <img src="/img/api/upload_resource.png" width="80%" />
 </p>

## Create a Workflow

<p align="center">
   <img src="/img/api/create_workflow1.png" width="80%" />
 </p>


<p align="center">
   <img src="/img/api/create_workflow2.png" width="80%" />
 </p>


<p align="center">
   <img src="/img/api/create_workflow3.png" width="80%" />
 </p>


<p align="center">
   <img src="/img/api/create_workflow4.png" width="80%" />
 </p>

## View the Execution Result

<p align="center">
   <img src="/img/api/execution_result.png" width="80%" />
 </p>

## View Log

<p align="center">
   <img src="/img/api/log.png" width="80%" />
 </p>