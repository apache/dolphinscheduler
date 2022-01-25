.. Licensed to the Apache Software Foundation (ASF) under one
   or more contributor license agreements.  See the NOTICE file
   distributed with this work for additional information
   regarding copyright ownership.  The ASF licenses this file
   to you under the Apache License, Version 2.0 (the
   "License"); you may not use this file except in compliance
   with the License.  You may obtain a copy of the License at

..   http://www.apache.org/licenses/LICENSE-2.0

.. Unless required by applicable law or agreed to in writing,
   software distributed under the License is distributed on an
   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
   KIND, either express or implied.  See the License for the
   specific language governing permissions and limitations
   under the License.

Concepts
========

In this section, you would know the core concepts of *PyDolphinScheduler*.

Process Definition
------------------

Process definition describe the whole things except `tasks`_ and `tasks dependence`_, which including
name, schedule interval, schedule start time and end time. You would know scheduler 

Process definition could be initialized in normal assign statement or in context manger.

.. code-block:: python

   # Initialization with assign statement
   pd = ProcessDefinition(name="my first process definition")

   # Or context manger 
   with ProcessDefinition(name="my first process definition") as pd:
       pd.submit()

Process definition is the main object communicate between *PyDolphinScheduler* and DolphinScheduler daemon.
After process definition and task is be declared, you could use `submit` and `run` notify server your definition.

If you just want to submit your definition and create workflow, without run it, you should use attribute `submit`.
But if you want to run the workflow after you submit it, you could use attribute `run`.

.. code-block:: python

   # Just submit definition, without run it
   pd.submit()
   
   # Both submit and run definition
   pd.run()

Schedule
~~~~~~~~

We use parameter `schedule` determine the schedule interval of workflow, *PyDolphinScheduler* support seven
asterisks expression, and each of the meaning of position as below

.. code-block:: text

    * * * * * * *
    ┬ ┬ ┬ ┬ ┬ ┬ ┬
    │ │ │ │ │ │ │
    │ │ │ │ │ │ └─── year
    │ │ │ │ │ └───── day of week (0 - 7) (0 to 6 are Sunday to Saturday, or use names; 7 is Sunday, the same as 0)
    │ │ │ │ └─────── month (1 - 12)
    │ │ │ └───────── day of month (1 - 31)
    │ │ └─────────── hour (0 - 23)
    │ └───────────── min (0 - 59)
    └─────────────── second (0 - 59)

Here we add some example crontab:

- `0 0 0 * * ? *`: Workflow execute every day at 00:00:00.
- `10 2 * * * ? *`: Workflow execute hourly day at ten pass two.
- `10,11 20 0 1,2 * ? *`: Workflow execute first and second day of month at 00:20:10 and 00:20:11.

Tenant
~~~~~~

Tenant is the user who run task command in machine or in virtual machine. it could be assign by simple string.

.. code-block:: python

   # 
   pd = ProcessDefinition(name="process definition tenant", tenant="tenant_exists")

.. note::

   Make should tenant exists in target machine, otherwise it will raise an error when you try to run command

Tasks
-----

Task is the minimum unit running actual job, and it is nodes of DAG, aka directed acyclic graph. You could define
what you want to in the task. It have some required parameter to make uniqueness and definition.

Here we use :py:meth:`pydolphinscheduler.tasks.Shell` as example, parameter `name` and `command` is required and must be provider. Parameter
`name` set name to the task, and parameter `command` declare the command you wish to run in this task.

.. code-block:: python

   # We named this task as "shell", and just run command `echo shell task`
   shell_task = Shell(name="shell", command="echo shell task")

If you want to see all type of tasks, you could see :doc:`tasks/index`.

Tasks Dependence
~~~~~~~~~~~~~~~~

You could define many tasks in on single `Process Definition`_. If all those task is in parallel processing,
then you could leave them alone without adding any additional information. But if there have some tasks should
not be run unless pre task in workflow have be done, we should set task dependence to them. Set tasks dependence
have two mainly way and both of them is easy. You could use bitwise operator `>>` and `<<`, or task attribute 
`set_downstream` and `set_upstream` to do it.

.. code-block:: python

   # Set task1 as task2 upstream
   task1 >> task2
   # You could use attribute `set_downstream` too, is same as `task1 >> task2`
   task1.set_downstream(task2)
   
   # Set task1 as task2 downstream
   task1 << task2
   # It is same as attribute `set_upstream`
   task1.set_upstream(task2)
   
   # Beside, we could set dependence between task and sequence of tasks,
   # we set `task1` is upstream to both `task2` and `task3`. It is useful
   # for some tasks have same dependence.
   task1 >> [task2, task3]

Task With Process Definition
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In most of data orchestration cases, you should assigned attribute `process_definition` to task instance to
decide workflow of task. You could set `process_definition` in both normal assign or in context manger mode

.. code-block:: python

   # Normal assign, have to explicit declaration and pass `ProcessDefinition` instance to task
   pd = ProcessDefinition(name="my first process definition")
   shell_task = Shell(name="shell", command="echo shell task", process_definition=pd)

   # Context manger, `ProcessDefinition` instance pd would implicit declaration to task
   with ProcessDefinition(name="my first process definition") as pd:
       shell_task = Shell(name="shell", command="echo shell task",

With both `Process Definition`_, `Tasks`_  and `Tasks Dependence`_, we could build a workflow with multiple tasks.
