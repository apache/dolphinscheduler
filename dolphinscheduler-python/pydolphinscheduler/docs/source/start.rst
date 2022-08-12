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

Getting Started
===============

To get started with *PyDolphinScheduler* you must ensure python and pip
installed on your machine, if you're already set up, you can skip straight
to `Installing PyDolphinScheduler`_, otherwise please continue with
`Installing Python`_.

Installing Python
-----------------

How to install `python` and `pip` depends on what operating system
you're using. The python wiki provides up to date
`instructions for all platforms here`_. When you entering the website
and choice your operating system, you would be offered the choice and
select python version. *PyDolphinScheduler* recommend use version above
Python 3.6 and we highly recommend you install *Stable Releases* instead
of *Pre-releases*.

After you have download and installed Python, you should open your terminal,
typing and running :code:`python --version` to check whether the installation
is correct or not. If all thing good, you could see the version in console
without error(here is a example after Python 3.8.7 installed)

.. code-block:: bash

   python --version

Will see detail of Python version, such as *Python 3.8.7*

Installing PyDolphinScheduler
-----------------------------

After Python is already installed on your machine following section
`installing Python`_, it easy to *PyDolphinScheduler* by pip.

.. code-block:: bash

   python -m pip install apache-dolphinscheduler

The latest version of *PyDolphinScheduler* would be installed after you run above
command in your terminal. You could go and `start Python Gateway Service`_ to finish
the prepare, and then go to :doc:`tutorial` to make your hand dirty. But if you
want to install the unreleased version of *PyDolphinScheduler*, you could go and see
section `installing PyDolphinScheduler in dev branch`_ for more detail.

.. note::

   Currently, we released multiple pre-release package in PyPI, you can see all released package
   including pre-release in `release history <https://pypi.org/project/apache-dolphinscheduler/#history>`_.
   You can fix the the package version if you want to install pre-release package, for example if
   you want to install version `3.0.0-beta-2` package, you can run command
   :code:`python -m pip install apache-dolphinscheduler==3.0.0b2`.

Installing PyDolphinScheduler In DEV Branch
-------------------------------------------

Because the project is developing and some of the features still not release.
If you want to try some thing unreleased you could install from the source code
which we hold in GitHub

.. code-block:: bash

   # Clone Apache DolphinScheduler repository
   git clone git@github.com:apache/dolphinscheduler.git
   # Install PyDolphinScheduler in develop mode
   cd dolphinscheduler-python/pydolphinscheduler && python -m pip install -e .

After you installed *PyDolphinScheduler*, please remember `start Python Gateway Service`_
which waiting for *PyDolphinScheduler*'s workflow definition require.

Above command will clone whole dolphinscheduler source code to local, maybe you want to install latest pydolphinscheduler
package directly and do not care about other code(including Python gateway service code), you can execute command

.. code-block:: bash

   # Must escape the '&' character by adding '\' 
   pip install -e "git+https://github.com/apache/dolphinscheduler.git#egg=apache-dolphinscheduler&subdirectory=dolphinscheduler-python/pydolphinscheduler"

Start Python Gateway Service
----------------------------

Since **PyDolphinScheduler** is Python API for `Apache DolphinScheduler`_, it
could define workflow and tasks structure, but could not run it unless you
`install Apache DolphinScheduler`_ and start its API server which including
Python gateway service in it. We only and some key steps here and you could
go `install Apache DolphinScheduler`_ for more detail

.. code-block:: bash

   # Start DolphinScheduler api-server which including python gateway service
   ./bin/dolphinscheduler-daemon.sh start api-server

To check whether the server is alive or not, you could run :code:`jps`. And
the server is health if keyword `ApiApplicationServer` in the console.

.. code-block:: bash

   jps
   # ....
   # 201472 ApiApplicationServer
   # ....

.. note::

   Please make sure you already enabled started Python gateway service along with `api-server`. The configuration is in
   yaml config path `python-gateway.enabled : true` in api-server's configuration path in `api-server/conf/application.yaml`.
   The default value is true and Python gateway service start when api server is been started.

Run an Example
--------------

Before run an example for pydolphinscheduler, you should get the example code from it source code. You could run
single bash command to get it

.. code-block:: bash

   wget https://raw.githubusercontent.com/apache/dolphinscheduler/dev/dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/examples/tutorial.py

or you could copy-paste the content from `tutorial source code`_. And then you could run the example in your
terminal

.. code-block:: bash

   python tutorial.py

If you want to submit your workflow to a remote API server, which means that your workflow script is different
from the API server, you should first change pydolphinscheduler configuration and then submit the workflow script

.. code-block:: bash

   pydolphinscheduler config --init
   pydolphinscheduler config --set java_gateway.address <YOUR-API-SERVER-IP-OR-HOSTNAME>
   python tutorial.py

.. note::

   You could see more information in :doc:`config` about all the configurations pydolphinscheduler supported.

After that, you could go and see your DolphinScheduler web UI to find out a new workflow created by pydolphinscheduler,
and the path of web UI is `Project -> Workflow -> Workflow Definition`.


What's More
-----------

If you do not familiar with *PyDolphinScheduler*, you could go to :doc:`tutorial` and see how it works. But
if you already know the basic usage or concept of *PyDolphinScheduler*, you could go and play with all
:doc:`tasks/index` *PyDolphinScheduler* supports, or see our :doc:`howto/index` about useful cases.

.. _`instructions for all platforms here`: https://wiki.python.org/moin/BeginnersGuide/Download
.. _`Apache DolphinScheduler`: https://dolphinscheduler.apache.org
.. _`install Apache DolphinScheduler`: https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/installation/standalone.html
.. _`tutorial source code`: https://raw.githubusercontent.com/apache/dolphinscheduler/dev/dolphinscheduler-python/pydolphinscheduler/src/pydolphinscheduler/examples/tutorial.py
