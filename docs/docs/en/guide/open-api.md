# Open API

## Background

Generally, projects and processes are created through pages, but considering the integration with third-party systems requires API calls to manage projects and workflows.

## The Operation Steps of DolphinScheduler API Calls

### Create a Token

1. Log in to the scheduling system, click "Security", then click "Token manage" on the left, and click "Create token" to create a token.

<p align="center">
   <img src="/img/token-management-en.png" width="80%" />
 </p>

2. Select the "Expiration time" (Token validity time), select "User" (choose the specified user to perform the API operation), click "Generate token", copy the `Token` string, and click "Submit".

<p align="center">
   <img src="/img/create-token-en1.png" width="80%" />
 </p>

### Token Usage

1. Open the API documentation page
    > Address：http://{API server ip}:12345/dolphinscheduler/doc.html?language=en_US&lang=en
<p align="center">
   <img src="/img/api-documentation-en.png" width="80%" />
 </p>
 
2. select a test API, the API selected for this test is `queryAllProjectList`
    > projects/query-project-list
3. Open `Postman`, fill in the API address, enter the `Token` in `Headers`, and then send the request to view the result:
    ```
    token: The Token just generated
    ```
<p align="center">
   <img src="/img/test-api.png" width="80%" />
 </p>  

### Create a Project

Here is an example of creating a project named "wudl-flink-test"：
<p align="center">
   <img src="/img/api/create_project1.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/api/create_project2.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/api/create_project3.png" width="80%" />
 </p>
The returned `msg` information is "success", indicating that we have successfully created the project through API.

If you are interested in the source code of creating a project, please continue to read the following：

### Appendix： The Source Code of Creating a Project

<p align="center">
   <img src="/img/api/create_source1.png" width="80%" />
 </p>

<p align="center">
   <img src="/img/api/create_source2.png" width="80%" />
 </p>


