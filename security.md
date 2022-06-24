# Security (Authorization System)


*   Only the administrator account in the security center has permission to operate, which includes queue management, tenant management, user management, alarm group management, worker group management, token management and other functions. In the user management module, you can authorize resources, data sources, projects, etc.
*   Administrator login, default username/password: `admin/dolphinscheduler123`

Create queue
------------

*   The queue is used when executing programs such as spark and mapreduce, and the "queue" parameter needs to be used.
*   The administrator enters the `Security Center` -> `Queue Management` page, and clicks the "`Create Queue`" button to create a queue.

![create-queue](/img/new_ui/dev/security/create-queue.png)

Add tenant
----------

*   The tenant corresponds to the Linux user, which is used by the worker to submit the job. If linux does not have this user, it will cause the task to fail. You can automatically create a linux user when the user does not exist by modifying the parameters in the `worker.properties`configuration file. The parameter will require that the worker can run the command

                                                                                       `worker.tenant.auto.create = true``worker.tenant.auto.create = true``sudo`

*   Tenant code: **The tenant code is the user on Linux, unique and cannot be repeated**
*   The administrator enters the `Security Center` -> `Tenant Management` page, and clicks the "`Create Tenant`" button to create a tenant.

![create-tenant](/img/new_ui/dev/security/create-tenant.png)

Create a normal user
--------------------

*   Users are divided into **administrator users** and **ordinary users**
    
    *   Administrators have permissions such as authorization and user management, but do not have permissions to create projects and actions defined by workflows.
    *   Ordinary users can create projects and create, edit, and execute workflow definitions.
    *   Note: If the user switches tenants, all resources under the user's tenant will be copied to the switched new tenant.
*   Go to the `Security Center` -> `User Management` page and click the "`Create User`" button to create a user.
    

![create-user](/img/new_ui/dev/security/create-user.png)

### Edit user information

*   The administrator enters the `Security Center` \-> `User Management` page, and clicks the "`Edit`" button to edit the user information.
*   After logging in as an ordinary user, click the user information in the drop-down box of the user name to enter the user information page, and click the "Edit" button to edit the user information.

### Modify user password

*   The administrator enters the `Security Center` -> `User Management` page, and clicks the "`Edit`" button. When editing user information, enter a new password to modify the user password.
*   After logging in as an ordinary user, click the user information in the user name drop-down box to enter the password modification page, enter the password and confirm the password and click the "Edit" button, then the password modification is successful.

Create an alert group
---------------------

*   The alarm group is a parameter set at startup. After the process ends, the process status and other information will be sent to the alarm group in the form of emails.
*   The administrator enters the `Security Center` -> `Alarm Group Management` page, and clicks the "`Create Alarm Group`" button to create an alarm group.

![create-alarmInstance](/img/new_ui/dev/security/create-alarmInstance.png)

Token management
----------------

Since the back-end interface has a login check, token management provides a way to perform various operations on the system by calling the interface.

*   The administrator enters the `Security Center` -> `Token Management` page, clicks the "`Create Token`" button, selects the expiration time and user, clicks the "`Generate Token`" button, and clicks the "`Submit`" button, then the user's token is successfully created.

![create-token](/img/new_ui/dev/security/create-token.png)

*   After the ordinary user logs in, click the user information in the drop-down box of the user name, enter the token management page, select the expiration time, click the "`Generate Token`" button, and click the "`Submit`" button, then the user successfully creates the token.
    
*   Example of calling:
    

```java
    /**
     * test token
     */
    public  void doPOSTParam()throws Exception{
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // create http post request
        HttpPost httpPost = new HttpPost("http://127.0.0.1:12345/escheduler/projects/create");
        httpPost.setHeader("token", "123");
        // set parameters
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("projectName", "qzw"));
        parameters.add(new BasicNameValuePair("desc", "qzw"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            // execute
            response = httpclient.execute(httpPost);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }
```

Granted permission
------------------

*   Granted permissions include project permissions, resource permissions, data source permissions, and UDF function permissions.
*   Administrators can authorize projects, resources, data sources, and UDF functions that ordinary users do not create. Because the authorization methods of projects, resources, data sources and UDF functions are all the same, the project authorization is used as an example to introduce.
*   Note: For projects created by the user, the user has all permissions. The item list and the selected items list are not displayed.

*   The administrator enters the `Security Center` -> `User Management` page, and clicks the "Authorize" button of the user to be authorized, as shown in the following figure:

![user-authorize](/img/new_ui/dev/security/user-authorize.png)

*   Select the project to authorize the project.

![project-authorize](/img/new_ui/dev/security/project-authorize.png)

*   Resource, data source, and UDF function authorization are the same as project authorization.

Worker grouping
---------------

Each worker node will belong to its own worker group, and the default group is default.

When the task is executed, the task can be assigned to the specified worker group, and finally the worker node in the group will execute the task.

> ### Add/update worker group

*   Open the `conf/worker.properties`configuration . Modify the worker.groups parameter.
*   The worker.groups parameter corresponds to the group name corresponding to the worker node, and the default is default.
*   If the worker node corresponds to multiple groups, separate them with commas. Example,

```conf
worker.groups=default,test
```

*   You can also modify the worker group to which the worker belongs during operation. If the modification is successful, the worker will use the newly created group, `worker.properties`ignoring the configuration in . The modification steps are "`Security Center` -> `Worker Group Management` -> Click '`New Worker Group`' -> Enter '`Group Name`' -> Select an existing worker -> Click '`Submit`'"

Environmental management
------------------------

*   Configure the worker running environment online, a worker can specify multiple environments, each environment is equivalent to the dolphinscheduler\_env.sh file.
    
*   The default environment is the dolphinscheduler\_env.sh file.
    
*   When the task is executed, the task can be assigned to the specified worker group, and the corresponding environment can be selected according to the worker group, and finally the worker node in the group executes the environment and then executes the task.
    

> ### Create/update environment

*   The environment configuration is equivalent to the configuration in the dolphinscheduler\_env.sh file

![create-environment](/img/new_ui/dev/security/create-environment.png)

> ### Use environment

*   Create a task node in the workflow definition and select the worker group and the environment corresponding to the worker group. When the task is executed, the worker will first execute the environment before executing the task.

![use-environment](/img/new_ui/dev/security/use-environment.png)