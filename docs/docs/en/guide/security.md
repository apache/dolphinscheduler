# Security (Authorization System)

* Only the administrator account in the security center has the authority to operate. It has functions such as queue management, tenant management, user management, alarm group management, worker group management, token management, etc. In the user management module, can authorize to the resources, data sources, projects, etc.
* Administrator login, the default username and password is `admin/dolphinscheduler123`

## Create Queue

- Configure `queue` parameter to execute programs such as Spark and MapReduce.
- The administrator enters the `Security Center->Queue Management` page and clicks the "Create Queue" button to create a new queue.

![create-queue](/img/new_ui/dev/security/create-queue.png)

## Add Tenant

- The tenant corresponds to the Linux user, which is used by the worker to submit the job. The task will fail if Linux does not have this user exists. You can set the parameter `worker.tenant.auto.create` as `true` in configuration file `worker.properties`. After that DolphinScheduler will create a user if not exists, The property `worker.tenant.auto.create=true` requests worker run `sudo` command without password.
- Tenant Code: **Tenant Code is the only user on Linux and cannot be repeated**
- The administrator enters the `Security Center->Tenant Management` page and clicks the `Create Tenant` button to create a tenant.

![create-tenant](/img/new_ui/dev/security/create-tenant.png)

## Create Normal User

- Users are divided into **administrator users** and **normal users**

  - The administrator has authorization to authorize and user management authorities but does not have the authority to create project and workflow definition operations.
  - Normal users can create projects and create, edit and execute workflow definitions.
  - **Note**: If the user switches tenants, all resources under the tenant to which the user belongs will be copied to the new tenant that is switched.

- The administrator enters the `Security Center -> User Management` page and clicks the `Create User` button to create a user.

![create-user](/img/new_ui/dev/security/create-user.png)

> **Edit user information**

- The administrator enters the `Security Center->User Management` page and clicks the `Edit` button to edit user information.
- After a normal user logs in, click the user information in the username drop-down box to enter the user information page, and click the `Edit` button to edit the user information.

> **Modify user password**

- The administrator enters the `Security Center->User Management` page and clicks the `Edit` button. When editing user information, enter the new password to modify the user password.
- After a normal user logs in, click the user information in the username drop-down box to enter the password modification page, enter the password and confirm the password and click the `Edit` button, then the password modification is a success.

## Create Alarm Group

- The alarm group is a parameter set at startup. After the process ends, the status of the process and other information will be sent to the alarm group by email.

* The administrator enters the `Security Center -> Alarm Group Management` page and clicks the `Create Alarm Group` button to create an alarm group.

![create-alarmInstance](/img/new_ui/dev/security/create-alarmInstance.png)

## Token Management

> Since the back-end interface has login check, token management provides a way to execute various operations on the system by calling interfaces.

- The administrator enters the `Security Center -> Token Management page`, clicks the `Create Token` button, selects the expiration time and user, clicks the `Generate Token` button, and clicks the `Submit` button, then create the selected user's token successfully.

![create-token](/img/new_ui/dev/security/create-token.png)

- After a normal user logs in, click the user information in the username drop-down box, enter the token management page, select the expiration time, click the `Generate Token` button, and click the `Submit` button, then the user creates a token successfully.
- Call example:

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

## Granted Permissions

    * Granted permissions include project permissions, resource permissions, data source permissions, UDF function permissions.
    * The administrator can authorize the projects, resources, data sources and UDF functions to normal users which not created by them. Because the way to authorize projects, resources, data sources and UDF functions to users is the same, we take project authorization as an example.
    * Note: The user has all permissions to the projects created by them. Projects will not be displayed in the project list and the selected project list.

- The administrator enters the `Security Center -> User Management` page and clicks the `Authorize` button of the user who needs to be authorized, as shown in the figure below:
 <p align="center">
  <img src="/img/auth-en.png" width="80%" />
</p>

- Select the project and authorize the project.

<p align="center">
   <img src="/img/auth-project-en.png" width="80%" />
 </p>

- Resources, data sources, and UDF function authorization are the same as project authorization.

## Worker Grouping

Each worker node will belong to its own worker group, and the default group is "default".

When executing a task, the task can be assigned to the specified worker group, and the task will be executed by the worker node in the group.

> Add or update worker group

- Open the `conf/worker.properties` configuration file on the worker node where you want to configure the groups and modify the `worker.groups` parameter.
- The `worker.groups` parameter is followed by the name of the group corresponding to the worker node, which is `default`.
- If the worker node corresponds to more than one group, they are separated by commas.

```conf
worker.groups=default,test
```
- You can also change the worker group for the worker during execution, and if the modification is successful, the worker will use the new group and ignore the configuration in `worker.properties`. The step to modify work group as below: `Security Center -> Worker Group Management -> click 'New Worker Group' -> click 'New Worker Group' ->  enter 'Group Name' -> Select Exists Worker -> Click Submit`. 

## Environmental Management

* Configure the Worker operating environment online. A Worker can specify multiple environments, and each environment is equivalent to the `dolphinscheduler_env.sh` file.

* The default environment is the `dolphinscheduler_env.sh` file.

* When executing a task, the task can be assigned to the specified worker group, and select the corresponding environment according to the worker group. Finally, the worker node executes the environment first and then executes the task.

> Add or update environment

- The environment configuration is equivalent to the configuration in the `dolphinscheduler_env.sh` file.

![create-environment](/img/new_ui/dev/security/create-environment.png)

> Usage environment

- Create a task node in the workflow definition, select the worker group and the environment corresponding to the worker group. When executing the task, the Worker will execute the environment first before executing the task.

![use-environment](/img/new_ui/dev/security/use-environment.png)

## Namespace Management

> Add or update k8s cluster

- First enter the configuration of the k8s cluster connection into the table `t_ds_k8s` in the database, which will be configured later by the web page.

> Add or update namespace

- After creation and authorization, you can select it from the namespace drop down list when edit k8s task, If the k8s cluster name is `ds_null_k8s` means test mode which will not operate the cluster actually.

![create-environment](/img/new_ui/dev/security/create-namespace.png)
