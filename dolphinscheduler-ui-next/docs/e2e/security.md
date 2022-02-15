### security

class:
- [ ] tab-tenant-manage
- [ ] tab-user-manage
- [ ] tab-worker-group-manage
- [ ] tab-queue-manage
- [ ] tab-environment-manage
- [ ] tab-token-manage

#### tenant manage

class:
- [ ] items
- [ ] el-popconfirm
- [ ] el-button--primary
- [ ] tenantCode
- [ ] edit
- [ ] delete

id:
- [ ] btnCreateTenant
- [ ] inputTenantCode
- [ ] selectQueue
- [ ] inputDescription
- [ ] btnSubmit
- [ ] btnCancel

#### user manage

class:
- [ ] items
- [ ] el-popconfirm
- [ ] el-button--primary
- [ ] name
- [ ] edit
- [ ] delete

id:
- [ ] btnCreateUser
- [ ] inputUserName
- [ ] inputUserPassword
- [ ] selectTenant
- [ ] selectQueue
- [ ] inputEmail
- [ ] inputPhone
- [ ] radioStateEnable
- [ ] radioStateDisable
- [ ] btnSubmit
- [ ] btnCancel

#### worker group manage

class:
- [ ] items
- [ ] el-popconfirm
- [ ] el-button--primary
- [ ] vue-treeselect__menu
- [ ] name
- [ ] edit
- [ ] delete

id:
- [ ] btnCreateWorkerGroup
- [ ] inputWorkerGroupName
- [ ] selectWorkerAddress
- [ ] btnSubmit
- [ ] btnCancel

#### queue manage

class:
- [ ] items
- [ ] queueName
- [ ] edit

id:
- [ ] btnCreateQueue
- [ ] inputQueueName
- [ ] inputQueueValue
- [ ] btnSubmit
- [ ] btnCancel

#### environment manage

class:
- [ ] items
- [ ] el-popconfirm
- [ ] el-button--primary
- [ ] environmentName
- [ ] edit
- [ ] delete

id:
- [ ] btnCreateEnvironment
- [ ] inputEnvironmentName
- [ ] inputEnvironmentConfig
- [ ] inputEnvironmentDesc
- [ ] inputEnvironmentWorkerGroup
- [ ] btnSubmit
- [ ] btnCancel

#### token manage

| check              | class               |
|--------------------|---------------------|
| :white_check_mark: | items               |
|  | el-popconfirm       |
|  | el-button--primary  |
| :white_check_mark: | username            |
| :white_check_mark: | token               |
| :white_check_mark: | edit                |
| :white_check_mark: | delete              |
| :white_check_mark: | btn-create-token    |
| :white_check_mark: | btn-generate-token  |
| :white_check_mark: | btn-submit          |
| :white_check_mark: | btn-cancel          |
