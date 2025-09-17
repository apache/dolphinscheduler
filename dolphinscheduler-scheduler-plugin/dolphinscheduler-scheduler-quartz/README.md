# Introduction

This module is the quartz-scheduler plugin module, this plugin will use quartz as the scheduler.

# How to use

By default, you don't need to do any configuration, the quartz-scheduler use the same datasource with master.

```yaml
scheduler-plugin:
  quartz:
    # By default, will not use a separate datasource for quartz
    # If you want to use a separate datasource, set it to true and configure the datasource below
    # If set true and you do not configure hikari below, it will use the same configuration as spring.datasource.hikari
    using-separate-datasource: false
    # hikari:
```

## Use a separate datasource for quartz-scheduler

If your have a lot of workflow to be scheduled, it is better to set a separate datasource for quartz-scheduler,
otherwise the performance of quartz thread may be affected. After set `using-separate-datasource` to true, the
quartz-scheduler will use a new datasource, and the datasource configuration is same with the master datasource
configuration.

```yaml
scheduler-plugin:
  quartz:
    using-separate-datasource: true
```

If you want to use a separate datasource for quartz-scheduler, and set the configuration, you need to configure the
hikari configuration like below:

```yaml
scheduler-plugin:
  quartz:
    using-separate-datasource: true
    hikari:
      jdbc-url: jdbc:mysql://localhost:3306/dolphinscheduler
      username: root
      password: root
      minimum-idle: 8
      maximum-pool-size: 8
```

